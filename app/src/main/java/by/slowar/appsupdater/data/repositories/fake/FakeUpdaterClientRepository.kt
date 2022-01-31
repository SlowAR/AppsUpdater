package by.slowar.appsupdater.data.repositories.fake

import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.random.Random

@ScreenScope
class FakeUpdaterClientRepository @Inject constructor() : UpdaterRepository {

    private val appsForUpdateList = mutableListOf<UpdateAppData>()

    init {
        appsForUpdateList.apply {
            add(UpdateAppData("com.package.app1", "Some update description", "10 Mb"))
            add(UpdateAppData("com.package.app2", "Some update description", "20 Mb"))
            add(UpdateAppData("com.package.app3", "Some update description", "30 Mb"))
            add(UpdateAppData("com.package.app4", "Some update description", "40 Mb"))
            add(UpdateAppData("com.package.app5", "Some update description", "50 Mb"))
        }
    }

    override fun init(): Observable<Boolean> {
        val single = Single.create<Boolean> { it.onSuccess(true) }
        return single.toObservable()
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        return Single.create {
            it.onSuccess(appsForUpdateList[Random.nextInt(appsForUpdateList.size)])
        }
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppData>> {
        val single = Single.create<List<UpdateAppData>> {
            it.onSuccess(appsForUpdateList)
        }
        return single.toObservable()
    }

    override fun updateApp(packageName: String) {
        Log.e(Constants.LOG_TAG, "fake updateAppClick $packageName")
    }
}