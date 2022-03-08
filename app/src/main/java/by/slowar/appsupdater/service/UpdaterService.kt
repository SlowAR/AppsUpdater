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
        const val CANCEL_UPDATE = 1002
        const val CANCEL_ALL_UPDATES = 1003

        const val UPDATE_APP_STATUS = 2000

        const val CHECK_ALL_FOR_UPDATES_DATA = "CheckAllForUpdatesData"
        const val UPDATE_APP_DATA = "UpdateAppData"
        const val UPDATE_APP_STATUS_DATA = "UpdateAppStatusData"
        const val CANCEL_UPDATE_DATA = "CancelAppData"
        const val CANCEL_UPDATE_STATUS_DATA = "CancelAppData"
        const val LAST_UPDATE_APP = "LastUpdateApp"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var updateAppNotificationBuilder: NotificationCompat.Builder

    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.replyTo != null) {
                lastClientMessenger = msg.replyTo
            }
            activeRequests.add(msg.what)

            when (msg.what) {
                CHECK_ALL_FOR_UPDATES -> checkAllForUpdates(msg.data)
                UPDATE_APP -> installUpdate(msg.data)
                CANCEL_UPDATE -> cancelUpdate(msg.data)
                CANCEL_ALL_UPDATES -> cancelAllUpdates()
                else -> {
                    removeActiveRequest(msg.what)
                    super.handleMessage(msg)
                }
            }
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)
    private var lastClientMessenger: Messenger? = null

    private val activeRequests = mutableListOf<Int>()

    private var isForeground = false

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
        val packageNames = data.getStringArrayList(UPDATE_APP_DATA) ?: ArrayList()
        if (packageNames.isEmpty()) {
            return
        }

        serviceManager.updateApps(packageNames)
        if (!isForeground) {
            updateAppNotificationBuilder = getUpdateAppProgressNotificationBuilder(this)
            startForeground(NOTIFICATION_ID, updateAppNotificationBuilder.build())
            isForeground = true
        }
    }

    private fun cancelUpdate(data: Bundle) {
        val packageName = data.getString(CANCEL_UPDATE_DATA, "")
        serviceManager.cancelUpdate(packageName)
    }

    private fun cancelAllUpdates() {
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

    override fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun sendMessage(requestId: Int, data: Bundle?) {
        val removedRequestId = removeActiveRequest(requestId)
        if (removedRequestId == Constants.EMPTY) {
            return
        }

        val message = obtainMessage(requestId, data)
        Log.e(Constants.LOG_TAG, "Sending message to client: $message")
        lastClientMessenger?.send(message)
    }

    override fun sendStatusMessage(
        requestId: Int,
        statusId: Int,
        data: Bundle?,
        isLastMessage: Boolean
    ) {
        val message = obtainMessage(statusId, data)

        Log.e(Constants.LOG_TAG, "Sending status message to client: $message")

        var removedRequestId = Constants.EMPTY
        if (isLastMessage) {
            removedRequestId = removeActiveRequest(requestId)
        }

        if (!isLastMessage || removedRequestId != Constants.EMPTY) {
            lastClientMessenger?.send(message)
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

    private fun removeActiveRequest(id: Int): Int {
        val lastRequest = activeRequests.indexOf(id)
        if (lastRequest != Constants.EMPTY) {
            activeRequests.remove(id)
        }

        if (activeRequests.isEmpty() && isForeground) {
            stopForeground(false)
            isForeground = false
        }
        return lastRequest
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceManager.onClear()
    }
}