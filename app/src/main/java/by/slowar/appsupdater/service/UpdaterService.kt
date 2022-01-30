package by.slowar.appsupdater.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UpdaterService : Service() {

    companion object {

        const val CHECK_FOR_UPDATE = 1002
        const val INSTALL_UPDATE = 1001
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}