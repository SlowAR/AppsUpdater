package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.installedapps.AppsRepository
import by.slowar.appsupdater.data.installedapps.AppsRepositoryImpl
import by.slowar.appsupdater.data.updates.FakeUpdaterClientRepository
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.data.updates.UpdaterRepository
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import dagger.Binds
import dagger.Module

@Module
interface ClientRepositoryModule {

    @Binds
    @WorkingEntity
    fun bindWorkingUpdaterRepository(updaterRepository: UpdaterClientRepository): UpdaterRepository

    @Binds
    @FakeEntity
    fun bindFakeUpdaterRepository(updaterRepository: FakeUpdaterClientRepository): UpdaterRepository

    @Binds
    fun bindInstalledAppsRepository(appsRepository: AppsRepositoryImpl): AppsRepository
}