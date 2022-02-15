package by.slowar.appsupdater.data.installedapps

import by.slowar.appsupdater.data.installedapps.local.InstalledAppDto
import by.slowar.appsupdater.domain.InstalledApp

fun InstalledAppDto.toModel(): InstalledApp = InstalledApp(
    appName = this.appName,
    packageName = this.packageName,
    icon = this.icon
)