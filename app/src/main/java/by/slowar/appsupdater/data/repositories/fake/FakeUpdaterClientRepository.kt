package by.slowar.appsupdater.data.repositories.fake

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.data.models.UpdateAppState
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@ScreenScope
class FakeUpdaterClientRepository @Inject constructor() : UpdaterRepository {

    private val appsForUpdateList = mutableListOf<UpdateAppData>()

    init {
        appsForUpdateList.apply {
            add(UpdateAppData("com.package.app1", "Some update description", 10 * 1024 * 1024))
            add(UpdateAppData("com.package.app2", "Some update description", 20 * 1024 * 1024))
            add(UpdateAppData("com.package.app3", "Some update description", 30 * 1024 * 1024))
            add(UpdateAppData("com.package.app4", "Some update description", 40 * 1024 * 1024))
            add(UpdateAppData("com.package.app5", "Some update description", 50 * 1024 * 1024))
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

    override fun updateApp(packageName: String): Observable<UpdateAppState> {
        val appSize: Long = 7 * 1024 * 1024   //7MB

        return Observable.create { emitter ->
            emitter.onNext(UpdateAppState.InitializeState(packageName))
            TimeUnit.MILLISECONDS.sleep(Random.nextLong(100, 500))

            var downloadedBytes = 0L
            while (downloadedBytes < appSize) {
                TimeUnit.SECONDS.sleep(1)

                val downloadSpeedBytes =
                    Random.nextLong(500 * 1024, 2 * 1024 * 1024)   //500 Kb/s - 2 Mb/s
                downloadedBytes += downloadSpeedBytes.also {
                    it.coerceIn(0..appSize)
                }

                emitter.onNext(
                    UpdateAppState.DownloadingState(
                        packageName,
                        downloadedBytes,
                        appSize,
                        downloadSpeedBytes
                    )
                )
            }

            emitter.onNext(UpdateAppState.InstallingState(packageName))
            val installSpeed = 10 * 1024    //10 Kb per 1 Ms
            TimeUnit.MILLISECONDS.sleep(appSize / installSpeed)

            emitter.onNext(UpdateAppState.CompletedState(packageName))
            emitter.onComplete()
        }
    }
}