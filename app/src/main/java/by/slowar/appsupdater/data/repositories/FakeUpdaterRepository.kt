package by.slowar.appsupdater.data.repositories

import android.util.Log
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Single
import javax.inject.Inject
import kotlin.random.Random

@ScreenScope
class FakeUpdaterRepository @Inject constructor() : UpdaterRepository {

    private val appsForUpdateList = mutableListOf<UpdateAppData>()

    init {
        appsForUpdateList.apply {
            add(UpdateAppData("App1", "com.package.app1", "Some update description", "10 Mb"))
            add(UpdateAppData("App2", "com.package.app2", "Some update description", "20 Mb"))
            add(UpdateAppData("App3", "com.package.app3", "Some update description", "30 Mb"))
            add(UpdateAppData("App4", "com.package.app4", "Some update description", "40 Mb"))
            add(UpdateAppData("App5", "com.package.app5", "Some update description", "50 Mb"))
        }
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        return Single.create {
            it.onSuccess(appsForUpdateList[Random.nextInt(appsForUpdateList.size)])
        }
    }

    override fun updateApp(packageName: String) {
        Log.e("qweqwe", "fake updateAppClick $packageName")
    }
}