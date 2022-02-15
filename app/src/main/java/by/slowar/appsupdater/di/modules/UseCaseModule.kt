package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.data.updates.UpdaterRepository
import by.slowar.appsupdater.domain.use_cases.CheckForUpdatesUseCase
import dagger.Module
import dagger.Provides

@Module
object UseCaseModule {

    @Provides
    fun provideCheckForUpdatesUseCase(@WorkingEntity updaterRepository: UpdaterRepository): CheckForUpdatesUseCase {
        return CheckForUpdatesUseCase(updaterRepository)
    }
}