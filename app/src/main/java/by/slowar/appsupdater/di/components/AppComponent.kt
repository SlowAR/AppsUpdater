package by.slowar.appsupdater.di.components

import android.app.Application
import by.slowar.appsupdater.di.scopes.AppScope
import dagger.BindsInstance
import dagger.Component

@Component
@AppScope
interface AppComponent {

    fun getUpdatesListComponent(): UpdatesListComponent

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(appContext: Application): Builder

        fun build(): AppComponent
    }
}