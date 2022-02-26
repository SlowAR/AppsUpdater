package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.updates.AppUpdate
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@ScreenScope
class FakeUpdaterClientRepository @Inject constructor() : UpdaterClientRepository {

    private val appsForUpdateList = mutableListOf<AppUpdate>()

    init {
        appsForUpdateList.apply {
            add(AppUpdate("com.package.app1", "Some update description", 10 * 1024 * 1024))
            add(AppUpdate("com.package.app2", "Some update description", 20 * 1024 * 1024))
            add(AppUpdate("com.package.app3", "Some update description", 30 * 1024 * 1024))
            add(AppUpdate("com.package.app4", "Some update description", 40 * 1024 * 1024))
            add(AppUpdate("com.package.app5", "Some update description", 50 * 1024 * 1024))
        }
    }

    override fun checkForUpdates(packages: ArrayList<String>): Single<List<AppUpdate>> {
        return Single.create { emitter ->
            emitter.onSuccess(appsForUpdateList)
        }
    }

    override fun updateApps(packages: ArrayList<String>): Observable<AppUpdateItemStateDto> {
        return Observable.create { emitter ->
            val appSize: Long = 7 * 1024 * 1024   //7MB
            for (packageName in packages) {
                emitUpdateStates(emitter, packageName, appSize)
            }
        }
    }

    private fun emitUpdateStates(
        emitter: ObservableEmitter<AppUpdateItemStateDto>,
        packageName: String,
        appSize: Long
    ) {
        emitter.onNext(AppUpdateItemStateDto.Initializing(packageName))
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
                AppUpdateItemStateDto.Downloading(
                    packageName,
                    downloadedBytes,
                    appSize,
                    downloadSpeedBytes
                )
            )
        }

        emitter.onNext(AppUpdateItemStateDto.Installing(packageName))
        val installSpeed = 10 * 1024    //10 Kb per 1 Ms
        TimeUnit.MILLISECONDS.sleep(appSize / installSpeed)

        emitter.onNext(AppUpdateItemStateDto.CompletedResult(packageName))
        emitter.onComplete()
    }
}