package by.slowar.appsupdater.data.repositories.data_sources.local

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.service.UpdaterService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class UpdaterServiceDataSource @Inject constructor(private val appContext: Application) {

    private var bindServiceSource: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val checkForUpdatesSource: BehaviorSubject<List<UpdateAppData>> =
        BehaviorSubject.create()

    private val clientHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UpdaterService.CHECK_ALL_FOR_UPDATES -> handleAppsForUpdatesResult(msg.data)
                else -> super.handleMessage(msg)
            }
        }
    }

    private var serviceMessenger: Messenger? = null
    private val clientMessenger = Messenger(clientHandler)
    private val serviceIntent = Intent(appContext, UpdaterService::class.java)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            serviceMessenger = Messenger(binder)
            isBound = true
            bindServiceSource.onNext(true)
            Log.e(Constants.LOG_TAG, "Bound to updater service!")
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            serviceMessenger = null
            isBound = false
            bindServiceSource.onNext(false)
            Log.e(Constants.LOG_TAG, "Unbound from updater service!")
        }
    }

    private var isStarted: Boolean = false
    private var isBound: Boolean = false

    private fun startService() {
        if (!isStarted) {
            Log.e(Constants.LOG_TAG, "Starting updater service...")
            appContext.startService(serviceIntent)
            isStarted = true
        }
        if (!isBound) {
            Log.e(Constants.LOG_TAG, "Binding to updater service...")
            appContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopService() {
        if (isBound) {
            appContext.unbindService(serviceConnection)
            isBound = false
        }
        if (isStarted) {
            appContext.stopService(serviceIntent)
            isStarted = false;
        }
    }

    fun init(): Observable<Boolean> {
        startService()
        return bindServiceSource
    }

    fun checkAppForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("not implemented")
    }

    fun checkAllAppsForUpdates(packages: List<String>): Observable<List<UpdateAppData>> {
        val data = Bundle().apply {
            if (packages is ArrayList) {    //TODO refactor
                putStringArrayList(UpdaterService.CHECK_ALL_FOR_UPDATES_DATA, packages)
            } else {
                Log.e(Constants.LOG_TAG, "packages list should be an ArrayList")
            }
        }
        sendMessage(UpdaterService.CHECK_ALL_FOR_UPDATES, data, true)

        return checkForUpdatesSource
    }

    private fun handleAppsForUpdatesResult(data: Bundle) {
        Log.e(Constants.LOG_TAG, "Received apps for update from service")
        data.getParcelableArrayList<UpdateAppData>(UpdaterService.CHECK_ALL_FOR_UPDATES_DATA)?.let {
            Log.e(Constants.LOG_TAG, "Apps for update: ${it.size}")
            checkForUpdatesSource.onNext(it)
        }
    }

    private fun sendMessage(requestId: Int, data: Bundle? = null, needToReply: Boolean = false) {
        val message = Message.obtain(null, requestId)
        if (needToReply) {
            message.replyTo = clientMessenger
        }
        if (data != null) {
            message.data = data
        }

        Log.e(Constants.LOG_TAG, "Sending message to updater service: $message")

        serviceMessenger?.send(message)
    }

    fun updateApp(packageName: String) {
        TODO("not implemented")
    }
}