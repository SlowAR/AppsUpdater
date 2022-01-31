package by.slowar.appsupdater.data.repositories.fake

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.random.Random

class FakeUpdaterServiceRepository @Inject constructor() : UpdaterRepository {

    companion object {
        const val HAS_UPDATE_CHANCE = 0.2f
    }

    private val descriptions = mutableListOf(
        "Bug fixes, performance improvements",
        "Added a lot of new functionality and 2 new themes!",
        "",
        "Many bugs added, performance reduced",
        "Some information about the update, a lot of work has been done, but there is not much sense from this"
    )

    private val noDescriptionText = "The developer did not provide information"

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

                val hasUpdate = Random.nextFloat() <= HAS_UPDATE_CHANCE
                if (hasUpdate) {
                    val description = descriptions.random().ifEmpty { noDescriptionText }
                    val updateSize = Random.nextLong(100 * 1024, 100 * 1024 * 1024)     //100Kb - 100Mb
                    updateDataList.add(UpdateAppData(packageName, description, updateSize))
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