package by.slowar.appsupdater.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.di.components.DaggerUpdaterServiceComponent
import javax.inject.Inject

class UpdaterService : Service(), UpdaterServiceManager.Listener {

    companion object {

        const val NOTIFICATION_CHANNEL_ID = "UpdaterServiceNotificationChannelId"
        const val NOTIFICATION_ID = 10000

        const val CHECK_FOR_UPDATE = 1000
        const val CHECK_ALL_FOR_UPDATES = 1001
        const val INSTALL_UPDATE = 1002
        const val STOP_CURRENT_TASK = 1003

        const val CHECK_ALL_FOR_UPDATES_DATA = "CheckAllForUpdatesData"
    }

    private lateinit var notificationManager: NotificationManager

    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CHECK_FOR_UPDATE -> checkForUpdate()
                CHECK_ALL_FOR_UPDATES -> checkAllForUpdates(msg.data)
                INSTALL_UPDATE -> installUpdate()
                STOP_CURRENT_TASK -> stopCurrentTask()
                else -> super.handleMessage(msg)
            }
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)

    @Inject
    lateinit var serviceManager: UpdaterServiceManager

    override fun onCreate() {
        super.onCreate()
        Log.e(Constants.LOG_TAG, "Updater service onCreate()")
        DaggerUpdaterServiceComponent.create().inject(this)
        serviceManager.prepare(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(Constants.LOG_TAG, "Updater service onBind()")
        return serviceMessenger.binder
    }

    private fun checkForUpdate() {
    }

    private fun checkAllForUpdates(data: Bundle) {
        Log.e(Constants.LOG_TAG, "Updater service checkAllForUpdates()")
        data.getStringArrayList(CHECK_ALL_FOR_UPDATES_DATA)?.let { packages ->
            serviceManager.checkAllForUpdates(packages)
        }
    }

    private fun installUpdate() {
    }

    private fun stopCurrentTask() {
    }

    override fun sendMessage() {
        TODO("Not yet implemented")
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