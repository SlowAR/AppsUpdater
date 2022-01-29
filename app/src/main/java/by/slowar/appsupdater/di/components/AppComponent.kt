package by.slowar.appsupdater.di.components

import by.slowar.appsupdater.di.scopes.AppScope
import dagger.Component

@Component
@AppScope
interface AppComponent {

    fun getUpdatesListComponent(): UpdatesListComponent
}