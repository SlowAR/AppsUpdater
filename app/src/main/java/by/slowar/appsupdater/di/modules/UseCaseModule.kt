package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.data.installedapps.InstalledAppsRepository
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.updates.CheckForUpdatesUseCaseImpl
import dagger.Module
import dagger.Provides

@Module
object UseCaseModule {

    @Provides
    fun provideCheckForUpdatesUseCase(
        @WorkingEntity updaterRepository: UpdaterClientRepository,
        installedAppsRepository: InstalledAppsRepository
    ): CheckForUpdatesUseCaseImpl {
        return CheckForUpdatesUseCaseImpl(updaterRepository, installedAppsRepository)
    }
}