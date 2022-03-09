package by.slowar.appsupdater.data.updaterservice

import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.updates.remote.AppUpdateDto
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.random.Random

class FakeUpdaterServiceRepository @Inject constructor() : UpdaterServiceRepository {

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

    private var cachedAppsForUpdate = mutableListOf<AppUpdateDto>()
    private var currentlyUpdatingApp: String = ""

    private val updatingAppsSet = mutableSetOf<String>()
    private val updatesForCancelSet = mutableSetOf<String>()
    private val cancelUpdatesLock = ReentrantLock()

    override fun checkForUpdates(packages: List<String>): Single<List<AppUpdateDto>> {
        return if (cachedAppsForUpdate.isEmpty()) {
            generateAppsForUpdate(packages)
        } else {
            Single.just(cachedAppsForUpdate)
        }
    }

    private fun generateAppsForUpdate(packages: List<String>): Single<List<AppUpdateDto>> {
        return Single.create { emitter ->
            val updateDataList = mutableListOf<AppUpdateDto>()
            for (packageName in packages) {
                if (emitter.isDisposed) {
                    break
                }

                val hasUpdate = Random.nextFloat() <= HAS_UPDATE_CHANCE
                if (hasUpdate) {
                    val description = descriptions.random().ifEmpty { noDescriptionText }
                    val updateSize =
                        Random.nextLong(100 * 1024, 30 * 1024 * 1024)     //100Kb - 30Mb
                    updateDataList.add(AppUpdateDto(packageName, description, updateSize))
                }
            }

            cachedAppsForUpdate = updateDataList
            emitter.onSuccess(updateDataList)
        }
    }

    override fun updateApps(packages: List<String>): Observable<AppUpdateItemStateDto> {
        cancelUpdatesLock.lock()
        val updateObservables = mutableListOf<Observable<AppUpdateItemStateDto>>()
        for (packageName in packages) {
            val app = cachedAppsForUpdate.find { it.appPackage == packageName }
            val observable = if (app == null) {
                getErrorObservable(packageName, "App doesn't have update")
            } else {
                updatingAppsSet.add(packageName)
                Observable.create { emitter -> emulateAppUpdate(emitter, packageName, app) }
            }
            updateObservables.add(observable)
        }
        cancelUpdatesLock.unlock()
        return Observable.concat(updateObservables)
    }

    private fun getErrorObservable(packageName: String, errorMessage: String) =
        Observable.create<AppUpdateItemStateDto> { emitter ->
            emitter.onNext(
                AppUpdateItemStateDto.ErrorResult(
                    packageName,
                    IllegalStateException(errorMessage)
                )
            )
            emitter.onComplete()
        }

    private fun emulateAppUpdate(
        emitter: ObservableEmitter<AppUpdateItemStateDto>,
        packageName: String,
        app: AppUpdateDto
    ) {
        currentlyUpdatingApp = packageName
        if (checkForCancellation(packageName, emitter)) {
            completeAppUpdate(emitter, app, true)
            return
        }
        emitter.onNext(AppUpdateItemStateDto.Initializing(packageName))
        TimeUnit.MILLISECONDS.sleep(Random.nextLong(100, 500))

        var downloadedBytes = 0L
        while (downloadedBytes < app.updateSize) {
            if (checkForCancellation(packageName, emitter)) {
                completeAppUpdate(emitter, app, true)
                return
            }
            TimeUnit.SECONDS.sleep(1)

            val downloadSpeedBytes =
                Random.nextLong(500 * 1024, 3 * 1024 * 1024)   //500 Kb/s - 3 Mb/s
            downloadedBytes += downloadSpeedBytes.also {
                it.coerceIn(0..app.updateSize)
            }

            emitter.onNext(
                AppUpdateItemStateDto.Downloading(
                    app.appPackage,
                    downloadedBytes,
                    app.updateSize,
                    downloadSpeedBytes
                )
            )
        }

        if (checkForCancellation(packageName, emitter)) {
            completeAppUpdate(emitter, app, true)
            return
        }
        emitter.onNext(AppUpdateItemStateDto.Installing(app.appPackage))
        val installSpeed = 10 * 1024    //20 Kb per 1 Ms
        TimeUnit.MILLISECONDS.sleep(app.updateSize / installSpeed)

        if (checkForCancellation(packageName, emitter)) {
            completeAppUpdate(emitter, app, true)
            return
        }
        emitter.onNext(AppUpdateItemStateDto.CompletedResult(app.appPackage))

        completeAppUpdate(emitter, app)
    }

    private fun completeAppUpdate(
        emitter: ObservableEmitter<AppUpdateItemStateDto>,
        app: AppUpdateDto,
        isCancelled: Boolean = false
    ) {
        emitter.onComplete()
        currentlyUpdatingApp = ""

        cancelUpdatesLock.lock()
        updatingAppsSet.remove(app.appPackage)
        if (!isCancelled) {
            cachedAppsForUpdate.remove(app)
        }
        cancelUpdatesLock.unlock()

        Log.e(Constants.LOG_TAG, "App update completed on service!")
    }

    private fun checkForCancellation(
        currentPackage: String,
        emitter: ObservableEmitter<AppUpdateItemStateDto>
    ): Boolean {
        return if (removeCancelledApp(currentPackage)) {
            emitter.onNext(AppUpdateItemStateDto.CancelledResult(currentPackage))
            emitter.onComplete()
            true
        } else {
            false
        }
    }

    override fun cancelUpdate(packageName: String): Single<Boolean> {
        return Single.create { emitter ->
            cancelUpdatesLock.lock()
            when {
                packageName.isBlank() -> {
                    emitter.onError(IllegalArgumentException("Package name is blank!"))
                }
                else -> {
                    addAppForCancellation(packageName)
                    emitter.onSuccess(packageName == currentlyUpdatingApp)
                }
            }
            cancelUpdatesLock.unlock()
        }
    }

    override fun cancelAllUpdates(): Single<Unit> {
        return Single.just(addAllUpdatesForCancellation())
    }

    private fun addAppForCancellation(packageName: String) {
        cancelUpdatesLock.lock()
        updatesForCancelSet.add(packageName)
        cancelUpdatesLock.unlock()
    }

    private fun addAllUpdatesForCancellation() {
        cancelUpdatesLock.lock()
        updatesForCancelSet.addAll(updatingAppsSet)
        cancelUpdatesLock.unlock()
    }

    private fun removeCancelledApp(packageName: String): Boolean {
        cancelUpdatesLock.lock()
        return updatesForCancelSet.remove(packageName).also { cancelUpdatesLock.unlock() }
    }
}