package com.example.daily_photo_day

import com.example.daily_photo_day.ui.dialogs.DateTimePickerDialog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimePickerDialogTest {

    @Test
    fun testIsValidFutureDateReturnsFalseForFutureDate() {
        val futureDate = "15.12.2030 14:30"

        val result = DateTimePickerDialog.isValidFutureDate(futureDate)

        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsTrueForPastDate() {
        val pastDate = "15.12.2020 14:30"

        val result = DateTimePickerDialog.isValidFutureDate(pastDate)

        assertTrue(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForInvalidDate() {
        val invalidDate = "invalid_date"

        val result = DateTimePickerDialog.isValidFutureDate(invalidDate)

        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForEmptyString() {
        val emptyDate = ""

        val result = DateTimePickerDialog.isValidFutureDate(emptyDate)

        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForMalformedDate() {
        val malformedDate = "32.13.2023 25:70"

        val result = DateTimePickerDialog.isValidFutureDate(malformedDate)

        assertFalse(result)
    }
}