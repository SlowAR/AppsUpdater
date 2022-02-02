package by.slowar.appsupdater.service.utils

import android.content.Context
import androidx.core.app.NotificationCompat
import by.slowar.appsupdater.R
import by.slowar.appsupdater.service.UpdaterService
import by.slowar.appsupdater.utils.formatBytesValue

fun getCheckAllForUpdatesNotificationBuilder(
    context: Context,
    appsToUpdateAmount: Int
): NotificationCompat.Builder {
    val title = context.getString(R.string.updates_available_title)
    val text = "${context.getString(R.string.updates_available_text)}:$appsToUpdateAmount"
    return NotificationCompat.Builder(context, UpdaterService.NOTIFICATION_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_baseline_file_copy_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
}

fun getUpdateAppProgressNotificationBuilder(context: Context): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, UpdaterService.NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_get_app_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
}

fun refreshUpdateAppNotificationProgress(
    context: Context,
    notificationBuilder: NotificationCompat.Builder,
    packageName: String,
    downloaded: Long,
    total: Long,
    speed: Long
) {
    val downloadSpeed =
        "${formatBytesValue(speed, context)}${context.getString(R.string.per_second_word)}"
    val progress = (downloaded.toDouble() / total * 100).toInt()
    notificationBuilder.apply {
        setContentTitle(packageName)
        setContentText(downloadSpeed)
        setProgress(100, progress, false)
    }
}

fun getUpdateAppInstallingNotificationBuilder(
    context: Context,
    appName: String
): NotificationCompat.Builder {
    val title = context.getString(R.string.installing_text)
    val text = "${context.getString(R.string.installing_app_text)} $appName"
    return NotificationCompat.Builder(context, UpdaterService.NOTIFICATION_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_baseline_file_copy_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
}

fun getUpdateAppCompletedNotificationBuilder(
    context: Context,
    appName: String
): NotificationCompat.Builder {
    val title = context.getString(R.string.update_completed)
    val text = "$appName ${context.getString(R.string.successfully_updated)}"
    return NotificationCompat.Builder(context, UpdaterService.NOTIFICATION_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_baseline_check_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
}
