package by.slowar.appsupdater.di.components

import by.slowar.appsupdater.di.modules.ServiceRepositoryModule
import by.slowar.appsupdater.di.scopes.ServiceScope
import by.slowar.appsupdater.service.UpdaterService
import dagger.Component

@Component(modules = [ServiceRepositoryModule::class])
@ServiceScope
interface UpdaterServiceComponent {

    fun inject(service: UpdaterService)
}