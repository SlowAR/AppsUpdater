package by.slowar.appsupdater.service

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.updaterservice.UpdaterServiceRepository
import by.slowar.appsupdater.data.updates.remote.AppUpdateDto
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface UpdaterServiceManager {

    fun prepare(listener: UpdaterServiceManagerImpl.Listener)

    fun checkAllForUpdates(packages: List<String>)

    fun updateApps(packages: ArrayList<String>)

    fun onClear()
}

class UpdaterServiceManagerImpl @Inject constructor(
    @FakeEntity private val repository: UpdaterServiceRepository
) : UpdaterServiceManager {

    var hostListener: Listener? = null

    private var checkForUpdatesDisposable: Disposable? = null
    private var updateAppDisposable: Disposable? = null

    private var appsUnderUpdate: List<String> = emptyList()

    override fun prepare(listener: Listener) {
        hostListener = listener
    }

    override fun checkAllForUpdates(packages: List<String>) {
        if (checkForUpdatesDisposable != null) {
            return
        }

        checkForUpdatesDisposable = repository.checkForUpdates(packages)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { appsForUpdate ->
                    handleCheckAllForUpdateResponse(appsForUpdate)
                },
                { error ->
                    Log.e(Constants.LOG_TAG, "checkAllForUpdates: ${error.localizedMessage}")
                    checkForUpdatesDisposable = null
                }
            )
    }

    private fun handleCheckAllForUpdateResponse(appsForUpdate: List<AppUpdateDto>) {
        if (appsForUpdate.isNotEmpty()) {
            hostListener?.showAppsForUpdateInfo(appsForUpdate.size)
        }

        val data = Bundle().apply {
            putParcelableArrayList(
                UpdaterService.CHECK_ALL_FOR_UPDATES_DATA,
                appsForUpdate as ArrayList<out Parcelable>
            )
        }
        hostListener?.sendMessage(UpdaterService.CHECK_ALL_FOR_UPDATES, data)
    }

    override fun updateApps(packages: ArrayList<String>) {
        if (updateAppDisposable != null || appsUnderUpdate.isNotEmpty() || packages.isEmpty()) {
            return
        } else {
            appsUnderUpdate = packages
        }

        val updateObservables = mutableListOf<Observable<AppUpdateItemStateDto>>()
        for(packageName in packages) {
            updateObservables.add(repository.updateApp(packageName))
        }

        updateAppDisposable = Observable.concat(updateObservables)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updateState ->
                    handleUpdateAppStatus(updateState)
                },
                { error ->
                    Log.e(Constants.LOG_TAG, "updateApp: ${error.localizedMessage}")
                    updateAppDisposable = null
                },
                {
                    updateAppDisposable = null
                }
            )
    }

    private fun handleUpdateAppStatus(updateState: AppUpdateItemStateDto) {
        when (updateState) {
            is AppUpdateItemStateDto.Downloading -> hostListener?.showUpdateProgressInfo(
                updateState.packageName,
                updateState.downloadedBytes,
                updateState.totalBytes,
                updateState.downloadSpeedBytes
            )
            is AppUpdateItemStateDto.Installing -> hostListener?.showInstallingUpdateAppInfo(
                updateState.packageName
            )
            is AppUpdateItemStateDto.CompletedResult -> hostListener?.showCompletedUpdateAppInfo(
                updateState.packageName
            )
            else -> Log.e(Constants.LOG_TAG, updateState.toString())
        }

        val statusData = Bundle().apply {
            putParcelable(UpdaterService.UPDATE_APP_STATUS_DATA, updateState)
        }

        val isLastMessage =
            updateState is AppUpdateItemStateDto.CompletedResult || updateState is AppUpdateItemStateDto.ErrorResult
        hostListener?.sendStatusMessage(
            UpdaterService.UPDATE_APP,
            UpdaterService.UPDATE_APP_STATUS,
            statusData,
            isLastMessage
        )
    }

    override fun onClear() {
        checkForUpdatesDisposable?.dispose()
        updateAppDisposable?.dispose()
        hostListener = null
    }

    interface Listener {

        fun showAppsForUpdateInfo(appsForUpdateAmount: Int)

        fun showUpdateProgressInfo(packageName: String, downloaded: Long, total: Long, speed: Long)

        fun showInstallingUpdateAppInfo(appName: String)

        fun showCompletedUpdateAppInfo(appName: String)

        fun sendMessage(requestId: Int, data: Bundle?)

        fun sendStatusMessage(requestId: Int, statusId: Int, data: Bundle?, isLastMessage: Boolean)
    }
}