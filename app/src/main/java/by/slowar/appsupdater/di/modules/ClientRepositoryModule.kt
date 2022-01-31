package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.repositories.AppsRepositoryImpl
import by.slowar.appsupdater.data.repositories.fake.FakeUpdaterClientRepository
import by.slowar.appsupdater.data.repositories.UpdaterClientRepository
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.api.AppsRepository
import by.slowar.appsupdater.domain.api.UpdaterRepository
import dagger.Binds
import dagger.Module

@Module
abstract class ClientRepositoryModule {

    @Binds
    @WorkingEntity
    abstract fun bindWorkingUpdaterRepository(updaterRepository: UpdaterClientRepository): UpdaterRepository

    @Binds
    @FakeEntity
    abstract fun bindFakeUpdaterRepository(updaterRepository: FakeUpdaterClientRepository): UpdaterRepository

    @Binds
    abstract fun bindInstalledAppsRepository(appsRepository: AppsRepositoryImpl): AppsRepository
}