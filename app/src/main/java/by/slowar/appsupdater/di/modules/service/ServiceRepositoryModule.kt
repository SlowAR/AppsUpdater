package by.slowar.appsupdater.di.modules.service

import by.slowar.appsupdater.data.updaterservice.FakeUpdaterServiceRepository
import by.slowar.appsupdater.data.updaterservice.UpdaterServiceRepository
import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import dagger.Binds
import dagger.Module

@Module
interface ServiceRepositoryModule {

    @Binds
    @FakeEntity
    fun bindsFakeUpdaterRepository(repository: FakeUpdaterServiceRepository): UpdaterServiceRepository
}