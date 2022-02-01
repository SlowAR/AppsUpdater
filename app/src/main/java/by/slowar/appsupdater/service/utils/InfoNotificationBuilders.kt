package by.slowar.appsupdater.service.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import by.slowar.appsupdater.R
import by.slowar.appsupdater.service.UpdaterService
import by.slowar.appsupdater.ui.MainActivity

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

private fun getMainScreenPendingIntent(context: Context): PendingIntent {
    val mainScreenIntent = Intent(context, MainActivity::class.java)
    var flags = PendingIntent.FLAG_UPDATE_CURRENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flags = flags or PendingIntent.FLAG_IMMUTABLE
    }
    return PendingIntent.getActivity(context, 0, mainScreenIntent, flags)
}
