package by.slowar.appsupdater.di.components

import android.content.pm.PackageManager
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
        fun packageManager(packageManager: PackageManager): Builder

        fun build(): AppComponent
    }
}