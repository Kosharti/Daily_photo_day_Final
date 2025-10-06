package com.example.daily_photo_day.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateTimePickerDialog(
    private val context: Context,
    private val onDateTimeSelected: (String) -> Unit
) {
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun show(currentDate: String? = null) {
        currentDate?.let {
            try {
                val date = dateFormat.parse(it)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                calendar.time = Date()
            }
        }

        showDatePicker()
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    Toast.makeText(
                        context,
                        "Нельзя установить будущую дату",
                        Toast.LENGTH_SHORT
                    ).show()
                    calendar.time = Date()
                }

                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                val now = Calendar.getInstance()
                if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) &&
                    calendar.timeInMillis > now.timeInMillis
                ) {
                    Toast.makeText(
                        context,
                        "Нельзя установить будущее время для сегодняшней даты",
                        Toast.LENGTH_SHORT
                    ).show()
                    calendar.time = Date()
                }

                val selectedDateTime = dateFormat.format(calendar.time)
                onDateTimeSelected(selectedDateTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    companion object {
        fun isValidFutureDate(dateString: String): Boolean {
            return try {
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                format.isLenient = false
                val date = format.parse(dateString)
                date?.before(Date()) ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
}