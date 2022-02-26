package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.installedapps.InstalledAppsRepository
import by.slowar.appsupdater.data.installedapps.InstalledAppsRepositoryImpl
import by.slowar.appsupdater.data.updates.FakeUpdaterClientRepository
import by.slowar.appsupdater.data.updates.UpdaterClientRepositoryImpl
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import dagger.Binds
import dagger.Module

@Module
interface ClientRepositoryModule {

    @Binds
    @WorkingEntity
    fun bindWorkingUpdaterRepository(updaterRepository: UpdaterClientRepositoryImpl): UpdaterClientRepository

    @Binds
    @FakeEntity
    fun bindFakeUpdaterRepository(updaterRepository: FakeUpdaterClientRepository): UpdaterClientRepository

    @Binds
    fun bindInstalledAppsRepository(appsRepository: InstalledAppsRepositoryImpl): InstalledAppsRepository
}