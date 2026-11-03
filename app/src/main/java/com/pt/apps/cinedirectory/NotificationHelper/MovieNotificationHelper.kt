package com.pt.apps.cinedirectory.NotificationHelper

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pt.apps.cinedirectory.MainActivity

object MovieNotificationHelper {
    const val CHANNEL_TRENDING_ID = "channel_trending_movies"
    const val CHANNEL_RELEASES_ID = "channel_release_updates"

    private const val NOTIFICATION_ID_TRENDING = 1001
    private const val NOTIFICATION_ID_RELEASE = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val trendingChannel = NotificationChannel(
                CHANNEL_TRENDING_ID,
                "Trending Movie Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies about top-rated movies surging in popularity and IMDb charts."
            }

            val releasesChannel = NotificationChannel(
                CHANNEL_RELEASES_ID,
                "Movie Release Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates on newly remastered 4K releases, anniversaries, and sequels."
            }

            notificationManager.createNotificationChannel(trendingChannel)
            notificationManager.createNotificationChannel(releasesChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendTrendingAlert(
        context: Context,
        movieTitle: String,
        details: String,
        movieId: String = ""
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_MOVIE_ID", movieId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TRENDING,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRENDING_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("🔥 Trending on IMDb: $movieTitle")
            .setContentText(details)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$details\n\nRanked among the greatest films in cinematic history on IMDb. Tap to view ratings, cast, and plot summary.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TRENDING, builder.build())
        } catch (_: SecurityException) {
            // Permission might be denied
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendReleaseUpdate(
        context: Context,
        movieTitle: String,
        details: String,
        movieId: String = ""
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_MOVIE_ID", movieId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_RELEASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_RELEASES_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("🎬 Release Update: $movieTitle")
            .setContentText(details)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$details\n\nAdded to CineVault catalog with remastered audio and exclusive director commentary notes.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_RELEASE, builder.build())
        } catch (_: SecurityException) {
            // Permission might be denied
        }
    }
}
