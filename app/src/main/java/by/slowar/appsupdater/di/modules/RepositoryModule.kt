package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.repositories.AppsRepositoryImpl
import by.slowar.appsupdater.data.repositories.FakeUpdaterRepository
import by.slowar.appsupdater.data.repositories.UpdaterRepositoryImpl
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.api.AppsRepository
import by.slowar.appsupdater.domain.api.UpdaterRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    @WorkingEntity
    abstract fun bindsWorkingUpdaterRepository(updaterRepository: UpdaterRepositoryImpl): UpdaterRepository

    @Binds
    @FakeEntity
    abstract fun bindsFakeUpdaterRepository(updaterRepository: FakeUpdaterRepository): UpdaterRepository

    @Binds
    abstract fun bindsInstalledAppsRepository(appsRepository: AppsRepositoryImpl): AppsRepository
}