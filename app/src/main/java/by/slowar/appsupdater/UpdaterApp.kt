package by.slowar.appsupdater

import androidx.multidex.MultiDexApplication
import by.slowar.appsupdater.di.AppComponent
import by.slowar.appsupdater.di.DaggerAppComponent

class UpdaterApp : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}