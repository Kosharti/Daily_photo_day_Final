package com.example.daily_photo_day

import com.example.daily_photo_day.data.entity.PhotoPost
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.Calendar

class PhotoPostTest {

    @Test
    fun `getYear should return correct year from date string`() {
        // Arrange
        val post = PhotoPost(
            imageUri = "test_uri",
            title = "Test",
            description = "Test",
            location = "Test",
            date = "15.12.2023 14:30"
        )

        // Act
        val year = post.getYear()

        // Assert
        assertEquals(2023, year)
    }

    @Test
    fun `getYear should return current year for invalid date`() {
        // Arrange
        val post = PhotoPost(
            imageUri = "test_uri",
            title = "Test",
            description = "Test",
            location = "Test",
            date = "invalid_date"
        )
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Act
        val year = post.getYear()

        // Assert
        assertEquals(currentYear, year)
    }

    @Test
    fun `isValidDate should return true for valid date`() {
        // Act & Assert
        assertTrue(PhotoPost.Companion.isValidDate("15.12.2023 14:30"))
    }

    @Test
    fun `isValidDate should return false for invalid date`() {
        // Act & Assert
        assertFalse(PhotoPost.Companion.isValidDate("invalid_date"))
    }

    @Test
    fun `getCurrentDateTime should return current date in correct format`() {
        // Act
        val currentDateTime = PhotoPost.Companion.getCurrentDateTime()

        // Assert
        assertTrue(PhotoPost.Companion.isValidDate(currentDateTime))
    }
}