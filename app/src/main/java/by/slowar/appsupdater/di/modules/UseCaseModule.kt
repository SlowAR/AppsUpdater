package by.slowar.appsupdater.di.modules

import by.slowar.appsupdater.di.qualifiers.FakeEntity
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.api.UpdaterRepository
import by.slowar.appsupdater.domain.use_cases.CheckForUpdatesUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {

    @Provides
    fun provideCheckForUpdatesUseCase(@WorkingEntity updaterRepository: UpdaterRepository): CheckForUpdatesUseCase {
        return CheckForUpdatesUseCase(updaterRepository)
    }
}