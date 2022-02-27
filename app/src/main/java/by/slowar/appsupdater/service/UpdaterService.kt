package by.slowar.appsupdater.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.di.components.DaggerUpdaterServiceComponent
import by.slowar.appsupdater.service.utils.*
import javax.inject.Inject

class UpdaterService : Service(), UpdaterServiceManagerImpl.Listener {

    companion object {

        const val NOTIFICATION_CHANNEL_ID = "UpdaterServiceNotificationChannelId"
        const val NOTIFICATION_ID = 10000

        const val CHECK_ALL_FOR_UPDATES = 1000
        const val UPDATE_APP = 1001

        const val UPDATE_APP_STATUS = 2000

        const val CHECK_ALL_FOR_UPDATES_DATA = "CheckAllForUpdatesData"
        const val UPDATE_APP_DATA = "UpdateAppData"
        const val UPDATE_APP_STATUS_DATA = "UpdateAppStatusData"
        const val LAST_UPDATE_APP = "LastUpdateApp"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var updateAppNotificationBuilder: NotificationCompat.Builder

    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (activeRequesters.containsKey(msg.what)) {
                Log.e(Constants.LOG_TAG, "Request already running")
                super.handleMessage(msg)
                return
            }

            activeRequesters[msg.what] = msg.replyTo

            when (msg.what) {
                CHECK_ALL_FOR_UPDATES -> checkAllForUpdates(msg.data)
                UPDATE_APP -> installUpdate(msg.data)
                else -> {
                    activeRequesters.remove(msg.what)
                    super.handleMessage(msg)
                }
            }
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)

    private val activeRequesters = mutableMapOf<Int, Messenger>()

    @Inject
    lateinit var serviceManager: UpdaterServiceManager

    override fun onCreate() {
        super.onCreate()
        Log.e(Constants.LOG_TAG, "Updater service onCreate()")
        DaggerUpdaterServiceComponent.create().inject(this)
        serviceManager.prepare(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initNotificationChannel()
        updateAppNotificationBuilder = getUpdateAppProgressNotificationBuilder(this)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(Constants.LOG_TAG, "Updater service onBind()")
        return serviceMessenger.binder
    }

    private fun checkAllForUpdates(data: Bundle) {
        Log.e(Constants.LOG_TAG, "Updater service checkAllForUpdates()")
        data.getStringArrayList(CHECK_ALL_FOR_UPDATES_DATA)?.let { packages ->
            serviceManager.checkAllForUpdates(packages)
        }
    }

    private fun installUpdate(data: Bundle) {
        updateAppNotificationBuilder = getUpdateAppProgressNotificationBuilder(this)
        data.getStringArrayList(UPDATE_APP_DATA)?.let { packageName ->
            serviceManager.updateApps(packageName)
        }
    }

    override fun showAppsForUpdateInfo(appsForUpdateAmount: Int) {
        val notificationBuilder =
            getCheckAllForUpdatesNotificationBuilder(this, appsForUpdateAmount)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun showUpdateProgressInfo(
        packageName: String,
        downloaded: Long,
        total: Long,
        speed: Long
    ) {
        refreshUpdateAppNotificationProgress(
            this,
            updateAppNotificationBuilder,
            packageName,
            downloaded,
            total,
            speed
        )

        notificationManager.notify(NOTIFICATION_ID, updateAppNotificationBuilder.build())
    }

    override fun showInstallingUpdateAppInfo(appName: String) {
        val notificationBuilder = getUpdateAppInstallingNotificationBuilder(this, appName)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun showCompletedUpdateAppInfo(appName: String) {
        val notificationBuilder = getUpdateAppCompletedNotificationBuilder(this, appName)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun sendMessage(requestId: Int, data: Bundle?) {
        val message = obtainMessage(requestId, data)
        Log.e(Constants.LOG_TAG, "Sending message to client: $message")
        activeRequesters.remove(requestId)?.send(message)
    }

    override fun sendStatusMessage(
        requestId: Int,
        statusId: Int,
        data: Bundle?,
        isLastMessage: Boolean
    ) {
        val message = obtainMessage(statusId, data)

        Log.e(Constants.LOG_TAG, "Sending status message to client: $message")

        if (isLastMessage) {
            activeRequesters.remove(requestId)?.send(message)
        } else {
            activeRequesters[requestId]?.send(message)
        }
    }

    private fun obtainMessage(requestId: Int, data: Bundle?): Message {
        val message = Message.obtain(null, requestId)
        if (data != null) {
            message.data = data
        }
        return message
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "UpdaterServiceChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceManager.onClear()
    }
}