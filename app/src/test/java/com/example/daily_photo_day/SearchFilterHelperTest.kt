package com.example.daily_photo_day

import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.utils.SearchFilterHelper
import org.junit.Assert.*
import org.junit.Test

class SearchFilterHelperTest {

    private val testPosts = listOf(
        PhotoPost(
            id = 1,
            imageUri = "uri1",
            title = "Sunset at beach",
            description = "Beautiful sunset",
            location = "Bali",
            date = "15.12.2023 18:30"
        ),
        PhotoPost(
            id = 2,
            imageUri = "uri2",
            title = "Mountain hiking",
            description = "Amazing view",
            location = "Alps",
            date = "10.11.2023 12:00"
        ),
        PhotoPost(
            id = 3,
            imageUri = "uri3",
            title = "City night",
            description = "City lights",
            location = "New York",
            date = "20.12.2023 22:15"
        )
    )

    @Test
    fun `filterPosts should return posts matching search query`() {
        val result = SearchFilterHelper.filterPosts(testPosts, query = "sunset")

        assertEquals(1, result.size)
        assertEquals("Sunset at beach", result[0].title)
    }

    @Test
    fun `filterPosts should sort by date descending`() {
        val result = SearchFilterHelper.filterPosts(
            testPosts,
            sortBy = SearchFilterHelper.SortType.DATE_DESC
        )

        assertEquals("20.12.2023 22:15", result[0].date)
        assertEquals("15.12.2023 18:30", result[1].date)
        assertEquals("10.11.2023 12:00", result[2].date)
    }

    @Test
    fun `filterPosts should sort by title ascending`() {
        val result = SearchFilterHelper.filterPosts(
            testPosts,
            sortBy = SearchFilterHelper.SortType.TITLE_ASC
        )

        assertEquals("City night", result[0].title)
        assertEquals("Mountain hiking", result[1].title)
        assertEquals("Sunset at beach", result[2].title)
    }
}