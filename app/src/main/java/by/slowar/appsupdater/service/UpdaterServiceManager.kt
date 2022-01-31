package by.slowar.appsupdater.service

import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.domain.api.UpdaterRepository
import javax.inject.Inject

class UpdaterServiceManager @Inject constructor(@FakeEntity private val repository: UpdaterRepository) {

    var hostListener: Listener? = null

    fun prepare(listener: Listener) {
        hostListener = listener
    }

    fun checkAllForUpdates(packages: List<String>) {
        Log.e(Constants.LOG_TAG, "service got packages for checking updates! $packages")
    }

    fun onClear() {
        hostListener = null
    }

    interface Listener {
        fun sendMessage()
    }
}