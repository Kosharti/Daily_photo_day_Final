package com.example.daily_photo_day

import com.example.daily_photo_day.ui.dialogs.DateTimePickerDialog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimePickerDialogTest {

    @Test
    fun testIsValidFutureDateReturnsFalseForFutureDate() {
        // Arrange
        val futureDate = "15.12.2030 14:30"

        // Act
        val result = DateTimePickerDialog.isValidFutureDate(futureDate)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsTrueForPastDate() {
        // Arrange
        val pastDate = "15.12.2020 14:30"

        // Act
        val result = DateTimePickerDialog.isValidFutureDate(pastDate)

        // Assert
        assertTrue(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForInvalidDate() {
        // Arrange
        val invalidDate = "invalid_date"

        // Act
        val result = DateTimePickerDialog.isValidFutureDate(invalidDate)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForEmptyString() {
        // Arrange
        val emptyDate = ""

        // Act
        val result = DateTimePickerDialog.isValidFutureDate(emptyDate)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testIsValidFutureDateReturnsFalseForMalformedDate() {
        // Arrange
        val malformedDate = "32.13.2023 25:70"

        // Act
        val result = DateTimePickerDialog.isValidFutureDate(malformedDate)

        // Assert
        assertFalse(result)
    }
}