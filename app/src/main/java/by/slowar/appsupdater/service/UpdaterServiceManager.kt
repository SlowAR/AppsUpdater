package by.slowar.appsupdater.service

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.data.models.UpdateAppState
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdaterServiceManager @Inject constructor(@FakeEntity private val repository: UpdaterRepository) {

    var hostListener: Listener? = null

    private var checkForUpdatesDisposable: Disposable? = null
    private var updateAppDisposable: Disposable? = null

    private var appsForUpdateList: List<UpdateAppData> = emptyList()

    fun prepare(listener: Listener) {
        hostListener = listener
    }

    fun checkAllForUpdates(packages: List<String>) {
        //TODO fix disposables
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
                },
                {
                    checkForUpdatesDisposable = null
                }
            )
    }

    private fun handleCheckAllForUpdateResponse(appsForUpdate: List<UpdateAppData>) {
        appsForUpdateList = appsForUpdate
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

    fun updateApp(packageName: String) {
        if (updateAppDisposable != null) {
            return
        }

        updateAppDisposable = repository.updateApp(packageName)
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

    private fun handleUpdateAppStatus(updateState: UpdateAppState) {
        when (updateState) {
            is UpdateAppState.DownloadingState -> hostListener?.showUpdateProgressInfo(
                updateState.packageName,
                updateState.downloadedBytes,
                updateState.totalBytes,
                updateState.downloadSpeedBytes
            )
            is UpdateAppState.InstallingState -> hostListener?.showInstallingUpdateAppInfo(
                updateState.packageName
            )
            is UpdateAppState.CompletedState -> hostListener?.showCompletedUpdateAppInfo(
                updateState.packageName
            )
            else -> Log.e(Constants.LOG_TAG, updateState.toString())
        }

        val statusData = Bundle().apply {
            putParcelable(UpdaterService.UPDATE_APP_STATUS_DATA, updateState)
        }

        val isLastMessage =
            updateState is UpdateAppState.CompletedState || updateState is UpdateAppState.ErrorState
        hostListener?.sendStatusMessage(
            UpdaterService.UPDATE_APP,
            UpdaterService.UPDATE_APP_STATUS,
            statusData,
            isLastMessage
        )
    }

    fun onClear() {
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