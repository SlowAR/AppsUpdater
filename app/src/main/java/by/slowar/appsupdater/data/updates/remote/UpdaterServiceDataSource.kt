package by.slowar.appsupdater.data.updates.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.service.UpdaterService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject
import javax.inject.Inject

interface UpdaterServiceDataSource {

    fun init(): Single<Unit>

    fun checkAllAppsForUpdates(packages: ArrayList<String>): Single<List<AppUpdateDto>>

    fun updateApps(packages: ArrayList<String>): Observable<AppUpdateItemStateDto>
}

class UpdaterServiceDataSourceImpl @Inject constructor(
    private val appContext: Application
) : UpdaterServiceDataSource {

    private var bindServiceSource: SingleSubject<Unit> = SingleSubject.create()
    private val checkForUpdatesSource: SingleSubject<List<AppUpdateDto>> = SingleSubject.create()
    private var updateAppStatusSource: BehaviorSubject<AppUpdateItemStateDto> =
        BehaviorSubject.create()

    private val clientHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UpdaterService.CHECK_ALL_FOR_UPDATES -> handleAppsForUpdatesResult(msg.data)
                UpdaterService.UPDATE_APP_STATUS -> handleUpdateAppStatus(msg.data)
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
            bindServiceSource.onSuccess(Unit)
            Log.e(Constants.LOG_TAG, "Bound to updater service!")
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            serviceMessenger = null
            isBound = false
            bindServiceSource.onError(IllegalStateException("Unbound from updater service!"))
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
            isStarted = false
        }
    }

    override fun init(): Single<Unit> {
        if (!bindServiceSource.hasValue() || bindServiceSource.hasThrowable()) {
            bindServiceSource = SingleSubject.create()
            startService()
        }
        return bindServiceSource
    }

    override fun checkAllAppsForUpdates(packages: ArrayList<String>): Single<List<AppUpdateDto>> {
        val data = Bundle().apply {
            putStringArrayList(UpdaterService.CHECK_ALL_FOR_UPDATES_DATA, packages)
        }
        sendMessage(UpdaterService.CHECK_ALL_FOR_UPDATES, data, true)

        return checkForUpdatesSource
    }

    override fun updateApps(packages: ArrayList<String>): Observable<AppUpdateItemStateDto> {
        updateAppStatusSource = BehaviorSubject.create()
        val data = if (packages.isEmpty()) {
            null
        } else {
            Bundle().apply {
                putStringArrayList(UpdaterService.UPDATE_APP_DATA, packages)
            }
        }
        sendMessage(UpdaterService.UPDATE_APP, data, true)
        return updateAppStatusSource
    }

    private fun handleAppsForUpdatesResult(data: Bundle) {
        Log.e(Constants.LOG_TAG, "Received apps for update from service")
        data.getParcelableArrayList<AppUpdateDto>(UpdaterService.CHECK_ALL_FOR_UPDATES_DATA)?.let {
            Log.e(Constants.LOG_TAG, "Apps for update: ${it.size}")
            checkForUpdatesSource.onSuccess(it)
        }
            ?: checkForUpdatesSource.onError(IllegalStateException("Couldn't receive apps for update"))
    }

    private fun handleUpdateAppStatus(data: Bundle) {
        data.getParcelable<AppUpdateItemStateDto>(UpdaterService.UPDATE_APP_STATUS_DATA)
            ?.let { states ->
                Log.e(Constants.LOG_TAG, "handleUpdateAppStatus(): Receiving status message from service: $states")
                when (states) {
                    is AppUpdateItemStateDto.CompletedResult -> {
                        updateAppStatusSource.onNext(states)
                        val isLastApp = data.getBoolean(UpdaterService.LAST_UPDATE_APP, false)
                        if (isLastApp) {
                            updateAppStatusSource.onComplete()
                        }
                    }
                    is AppUpdateItemStateDto.ErrorResult -> updateAppStatusSource.onError(states.error)
                    else -> updateAppStatusSource.onNext(states)
                }
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
}