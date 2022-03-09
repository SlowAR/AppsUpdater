package by.slowar.appsupdater.di.components

import by.slowar.appsupdater.di.modules.updateslist.ClientRepositoryModule
import by.slowar.appsupdater.di.modules.DataSourceModule
import by.slowar.appsupdater.di.modules.UseCaseModule
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.ui.updates.UpdatesListFragment
import dagger.Subcomponent

@Subcomponent(modules = [ClientRepositoryModule::class, UseCaseModule::class, DataSourceModule::class])
@ScreenScope
interface UpdatesListComponent {

    fun inject(fragment: UpdatesListFragment)
}