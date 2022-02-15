package by.slowar.appsupdater.di.modules

import android.app.Application
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    fun providePackageManager(appContext: Application): PackageManager {
        return appContext.packageManager
    }
}