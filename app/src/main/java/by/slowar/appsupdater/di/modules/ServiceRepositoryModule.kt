package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.repositories.fake.FakeUpdaterServiceRepository
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.domain.api.UpdaterRepository
import dagger.Binds
import dagger.Module

@Module
abstract class ServiceRepositoryModule {

    @Binds
    @FakeEntity
    abstract fun bindsFakeUpdaterRepository(repository: FakeUpdaterServiceRepository): UpdaterRepository
}