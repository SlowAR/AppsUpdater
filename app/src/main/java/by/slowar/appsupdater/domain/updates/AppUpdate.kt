package by.slowar.appsupdater.domain.updates

data class AppUpdate(
    val appPackage: String,
    val description: String,
    val updateSize: Long
)