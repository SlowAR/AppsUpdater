package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.installedapps.local.InstalledAppsLocalDataSource
import by.slowar.appsupdater.data.installedapps.local.InstalledAppsLocalDataSourceImpl
import by.slowar.appsupdater.data.updates.remote.UpdaterServiceDataSource
import by.slowar.appsupdater.data.updates.remote.UpdaterServiceDataSourceImpl
import dagger.Binds
import dagger.Module

@Module
interface DataSourceModule {

    @Binds
    fun bindInstalledAppsLocalDataSource(dataSource: InstalledAppsLocalDataSourceImpl): InstalledAppsLocalDataSource

    @Binds
    fun bindUpdaterServiceDataSource(dataSource: UpdaterServiceDataSourceImpl): UpdaterServiceDataSource
}