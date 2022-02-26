package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.service.UpdaterServiceManager
import by.slowar.appsupdater.service.UpdaterServiceManagerImpl
import dagger.Binds
import dagger.Module

@Module
interface UpdaterServiceModule {

    @Binds
    fun bindUpdaterServiceManager(manager: UpdaterServiceManagerImpl): UpdaterServiceManager
}