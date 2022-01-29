package by.slowar.appsupdater

import androidx.multidex.MultiDexApplication
import by.slowar.appsupdater.di.components.AppComponent
import by.slowar.appsupdater.di.components.DaggerAppComponent

class UpdaterApp : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().packageManager(packageManager).build()
    }
}