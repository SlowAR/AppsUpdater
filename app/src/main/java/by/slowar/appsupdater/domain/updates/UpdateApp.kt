package by.slowar.appsupdater.domain.updates

data class UpdateApp(
    val appPackage: String,
    val description: String,
    val updateSize: Long
)