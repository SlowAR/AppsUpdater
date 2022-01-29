package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.repositories.UpdaterRepositoryImpl
import by.slowar.appsupdater.domain.UpdaterRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindsUpdaterRepository(updaterRepository: UpdaterRepositoryImpl): UpdaterRepository
}