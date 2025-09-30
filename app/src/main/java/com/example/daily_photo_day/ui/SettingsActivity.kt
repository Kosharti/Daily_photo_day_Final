package com.example.daily_photo_day.ui

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.daily_photo_day.databinding.ActivitySettingsBinding
import com.example.daily_photo_day.service.NotificationService
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    // Ключи для SharedPreferences
    companion object {
        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val PREF_NOTIFICATION_HOUR = "notification_hour"
        private const val PREF_NOTIFICATION_MINUTE = "notification_minute"
        private const val PREF_NOTIFICATION_TIME_TEXT = "notification_time_text"
    }

    // Регистрируем лаунчер для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение получено, включаем уведомления без показа часов
            enableNotificationsWithoutTimePicker()
        } else {
            // Разрешение не получено
            binding.switchNotifications.isChecked = false
            binding.textNotificationTime.text = "Разрешение на уведомления не предоставлено"
            saveNotificationSettings(false, -1, -1, "Разрешение на уведомления не предоставлено")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Включаем уведомления без показа часов
                checkNotificationPermission()
            } else {
                // Выключаем уведомления
                disableNotifications()
            }
        }

        // Добавляем кнопку для установки/изменения времени уведомления
        binding.buttonSetTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        val notificationsEnabled = sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, false)
        val hour = sharedPreferences.getInt(PREF_NOTIFICATION_HOUR, -1)
        val minute = sharedPreferences.getInt(PREF_NOTIFICATION_MINUTE, -1)
        val timeText = sharedPreferences.getString(PREF_NOTIFICATION_TIME_TEXT, "Уведомления отключены") ?: "Уведомления отключены"

        binding.switchNotifications.isChecked = notificationsEnabled
        binding.textNotificationTime.text = timeText

        // Управляем видимостью кнопки установки времени и текстом
        if (notificationsEnabled) {
            if (hour != -1 && minute != -1) {
                // Уведомления включены и время установлено
                binding.buttonSetTime.text = "Изменить время уведомления"
                binding.textNotificationTime.text = "Уведомление в ${String.format("%02d:%02d", hour, minute)}"
            } else {
                // Уведомления включены, но время не установлено
                binding.buttonSetTime.text = "Установить время уведомления"
                binding.textNotificationTime.text = "Время уведомления не установлено"
            }
            binding.buttonSetTime.visibility = View.VISIBLE
        } else {
            binding.buttonSetTime.visibility = View.GONE
        }
    }

    private fun enableNotificationsWithoutTimePicker() {
        val hour = sharedPreferences.getInt(PREF_NOTIFICATION_HOUR, -1)
        val minute = sharedPreferences.getInt(PREF_NOTIFICATION_MINUTE, -1)

        if (hour != -1 && minute != -1) {
            // Время уже установлено, просто включаем уведомления
            NotificationService.scheduleDailyNotification(this, hour, minute)
            binding.textNotificationTime.text = "Уведомление в ${String.format("%02d:%02d", hour, minute)}"
            binding.buttonSetTime.text = "Изменить время уведомления"
        } else {
            // Время не установлено, показываем статус
            binding.textNotificationTime.text = "Время уведомления не установлено"
            binding.buttonSetTime.text = "Установить время уведомления"
        }

        binding.buttonSetTime.visibility = View.VISIBLE
        saveNotificationSettings(true, hour, minute, binding.textNotificationTime.text.toString())
    }

    private fun disableNotifications() {
        NotificationService.cancelNotification(this)
        binding.textNotificationTime.text = "Уведомления отключены"
        binding.buttonSetTime.visibility = View.GONE
        saveNotificationSettings(false, -1, -1, "Уведомления отключены")
    }

    private fun saveNotificationSettings(enabled: Boolean, hour: Int, minute: Int, timeText: String) {
        with(sharedPreferences.edit()) {
            putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled)
            putInt(PREF_NOTIFICATION_HOUR, hour)
            putInt(PREF_NOTIFICATION_MINUTE, minute)
            putString(PREF_NOTIFICATION_TIME_TEXT, timeText)
            apply()
        }
    }

    private fun checkNotificationPermission() {
        // Для Android 13+ проверяем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Разрешение уже есть
                    enableNotificationsWithoutTimePicker()
                }
                else -> {
                    // Запрашиваем разрешение
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Для версий ниже Android 13 разрешение не требуется
            enableNotificationsWithoutTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val savedHour = sharedPreferences.getInt(PREF_NOTIFICATION_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
        val savedMinute = sharedPreferences.getInt(PREF_NOTIFICATION_MINUTE, calendar.get(Calendar.MINUTE))

        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Сохраняем настройки
                saveNotificationSettings(
                    enabled = true,
                    hour = hourOfDay,
                    minute = minute,
                    timeText = "Уведомление в ${String.format("%02d:%02d", hourOfDay, minute)}"
                )

                // Устанавливаем уведомление
                NotificationService.scheduleDailyNotification(
                    this,
                    hourOfDay,
                    minute
                )

                // Обновляем UI
                binding.textNotificationTime.text = "Уведомление в ${String.format("%02d:%02d", hourOfDay, minute)}"
                binding.buttonSetTime.text = "Изменить время уведомления"
                binding.switchNotifications.isChecked = true
                binding.buttonSetTime.visibility = View.VISIBLE
            },
            savedHour,
            savedMinute,
            true
        )
        timePicker.show()
    }

    override fun onPause() {
        super.onPause()
        // Сохраняем текущее состояние при выходе из активности
        if (binding.switchNotifications.isChecked) {
            val hour = sharedPreferences.getInt(PREF_NOTIFICATION_HOUR, -1)
            val minute = sharedPreferences.getInt(PREF_NOTIFICATION_MINUTE, -1)
            if (hour != -1 && minute != -1) {
                NotificationService.scheduleDailyNotification(this, hour, minute)
            }
        }
    }
}