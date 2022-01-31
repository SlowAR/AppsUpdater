package by.slowar.appsupdater.data.repositories.fake

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.random.Random

class FakeUpdaterServiceRepository @Inject constructor() : UpdaterRepository {

    override fun init(): Observable<Boolean> {
        return Observable.create {
            it.onNext(true)
            it.onComplete()
        }
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("Not yet implemented")
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppData>> {
        val updateDataList = mutableListOf<UpdateAppData>()
        return Observable.create { emitter ->
            for (packageName in packages) {
                if (emitter.isDisposed) {
                    break
                }

                val hasUpdate = Random.nextFloat() <= 0.2f
                if (hasUpdate) {
                    updateDataList.add(
                        UpdateAppData(
                            appPackage = packageName,
                            description = "Some description",
                            updateSize = "10 Mb"
                        )
                    )
                }
            }

            emitter.onNext(updateDataList)
            emitter.onComplete()
        }
    }

    override fun updateApp(packageName: String) {
        TODO("Not yet implemented")
    }
}