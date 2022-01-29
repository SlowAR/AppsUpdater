package by.slowar.appsupdater.di.components

import by.slowar.appsupdater.di.modules.RepositoryModule
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.ui.updates_list.UpdatesListFragment
import dagger.Subcomponent

@Subcomponent(modules = [RepositoryModule::class])
@ScreenScope
interface UpdatesListComponent {

    fun inject(fragment: UpdatesListFragment)
}