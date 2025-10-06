package com.example.daily_photo_day.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.daily_photo_day.MainActivity
import com.example.daily_photo_day.R
import java.util.Calendar

class NotificationService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)

        if (notificationsEnabled) {
            showNotification(context)
            rescheduleNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о фото",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о необходимости сделать ежедневное фото"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Ежедневный фотодневник")
            .setContentText("Не забудьте сделать сегодняшнее фото!")
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun rescheduleNotification(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val hour = sharedPreferences.getInt("notification_hour", -1)
        val minute = sharedPreferences.getInt("notification_minute", -1)

        if (hour != -1 && minute != -1) {
            scheduleDailyNotification(context, hour, minute)
        }
    }

    companion object {
        const val CHANNEL_ID = "photo_reminder_channel"
        const val NOTIFICATION_ID = 1

        fun scheduleDailyNotification(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun cancelNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}