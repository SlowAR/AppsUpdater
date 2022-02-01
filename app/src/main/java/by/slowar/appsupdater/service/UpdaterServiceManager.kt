package by.slowar.appsupdater.service

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdaterServiceManager @Inject constructor(@FakeEntity private val repository: UpdaterRepository) {

    var hostListener: Listener? = null

    private var currentTaskDisposable: Disposable? = null

    private var appsForUpdateList: List<UpdateAppData> = emptyList()

    fun prepare(listener: Listener) {
        hostListener = listener
    }

    fun checkAllForUpdates(packages: List<String>) {
        currentTaskDisposable = repository.checkForUpdates(packages)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { appsForUpdate ->
                    handleCheckAllForUpdateResponse(appsForUpdate)
                },
                { error ->
                    Log.e(Constants.LOG_TAG, "checkAllForUpdates: ${error.localizedMessage}")
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

    fun onClear() {
        currentTaskDisposable?.dispose()
        hostListener = null
    }

    interface Listener {

        fun showAppsForUpdateInfo(appsForUpdateAmount: Int)

        fun sendMessage(requestId: Int, data: Bundle?)
    }
}