package by.slowar.appsupdater.data.updaterservice

import by.slowar.appsupdater.data.updates.remote.UpdateAppDto
import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import by.slowar.appsupdater.data.updates.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
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

    private var cachedAppsForUpdate = emptyList<UpdateAppDto>()

    override fun init(): Observable<Boolean> {
        return Observable.create {
            it.onNext(true)
            it.onComplete()
        }
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppDto> {
        TODO("Not yet implemented")
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppDto>> {
        return Observable.create { emitter ->
            val updateDataList = mutableListOf<UpdateAppDto>()
            for (packageName in packages) {
                if (emitter.isDisposed) {
                    break
                }

                val hasUpdate = Random.nextFloat() <= HAS_UPDATE_CHANCE
                if (hasUpdate) {
                    val description = descriptions.random().ifEmpty { noDescriptionText }
                    val updateSize =
                        Random.nextLong(100 * 1024, 30 * 1024 * 1024)     //100Kb - 30Mb
                    updateDataList.add(UpdateAppDto(packageName, description, updateSize))
                }
            }

            cachedAppsForUpdate = updateDataList
            emitter.onNext(updateDataList)
            emitter.onComplete()
        }
    }

    override fun updateApp(packageName: String): Observable<UpdateAppState> {
        val app = cachedAppsForUpdate.find { it.appPackage == packageName }
        return if (app == null) {
            Observable.create { emitter ->
                emitter.onNext(
                    UpdateAppState.ErrorState(
                        packageName,
                        IllegalStateException("App doesn't have update")
                    )
                )
                emitter.onComplete()
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(UpdateAppState.InitializeState(packageName))
                TimeUnit.MILLISECONDS.sleep(Random.nextLong(100, 500))

                var downloadedBytes = 0L
                while (downloadedBytes < app.updateSize) {
                    TimeUnit.SECONDS.sleep(1)

                    val downloadSpeedBytes =
                        Random.nextLong(500 * 1024, 3 * 1024 * 1024)   //500 Kb/s - 3 Mb/s
                    downloadedBytes += downloadSpeedBytes.also {
                        it.coerceIn(0..app.updateSize)
                    }

                    emitter.onNext(
                        UpdateAppState.DownloadingState(
                            app.appPackage,
                            downloadedBytes,
                            app.updateSize,
                            downloadSpeedBytes
                        )
                    )
                }

                emitter.onNext(UpdateAppState.InstallingState(app.appPackage))
                val installSpeed = 10 * 1024    //20 Kb per 1 Ms
                TimeUnit.MILLISECONDS.sleep(app.updateSize / installSpeed)

                emitter.onNext(UpdateAppState.CompletedState(app.appPackage))
                emitter.onComplete()
            }
        }
    }
}