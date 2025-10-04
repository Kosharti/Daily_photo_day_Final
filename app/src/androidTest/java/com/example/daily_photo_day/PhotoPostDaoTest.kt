package com.example.daily_photo_day

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.daily_photo_day.data.AppDatabase
import com.example.daily_photo_day.data.dao.PhotoPostDao
import com.example.daily_photo_day.data.entity.PhotoPost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PhotoPostDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PhotoPostDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.photoPostDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrievePost() = runBlocking {
        // Arrange
        val post = PhotoPost(
            imageUri = "test_uri",
            title = "Test Post",
            description = "Test Description",
            location = "Test Location",
            date = "15.12.2023 14:30"
        )

        // Act
        val id = dao.insertPost(post)
        val retrievedPost = dao.getPostById(id)

        // Assert
        assertNotNull(retrievedPost)
        assertEquals("Test Post", retrievedPost?.title)
        assertEquals("Test Description", retrievedPost?.description)
    }

    @Test
    fun testUpdatePost() = runBlocking {
        // Arrange
        val post = PhotoPost(
            imageUri = "test_uri",
            title = "Original Title",
            description = "Original Description",
            location = "Original Location",
            date = "15.12.2023 14:30"
        )
        val id = dao.insertPost(post)
        val savedPost = dao.getPostById(id)!!

        // Act
        val updatedPost = savedPost.copy(title = "Updated Title")
        dao.updatePost(updatedPost)
        val result = dao.getPostById(id)

        // Assert
        assertEquals("Updated Title", result?.title)
    }

    @Test
    fun testDeletePost() = runBlocking {
        // Arrange
        val post = PhotoPost(
            imageUri = "test_uri",
            title = "Test Post",
            description = "Test Description",
            location = "Test Location",
            date = "15.12.2023 14:30"
        )
        val id = dao.insertPost(post)
        val savedPost = dao.getPostById(id)!!

        // Act
        dao.deletePost(savedPost)
        val result = dao.getPostById(id)

        // Assert
        assertNull(result)
    }

    @Test
    fun testGetAllPostsReturnsPostsInDescendingOrder() = runBlocking {
        // Arrange
        val post1 = PhotoPost(
            imageUri = "uri1",
            title = "Post 1",
            description = "Desc 1",
            location = "Loc 1",
            date = "10.12.2023 10:00"
        )
        val post2 = PhotoPost(
            imageUri = "uri2",
            title = "Post 2",
            description = "Desc 2",
            location = "Loc 2",
            date = "15.12.2023 14:30"
        )

        // Act
        dao.insertPost(post1)
        dao.insertPost(post2)
        val allPosts = dao.getAllPosts().first()

        // Assert
        assertEquals(2, allPosts.size)
        assertEquals("Post 2", allPosts[0].title) // Should be newest first
        assertEquals("Post 1", allPosts[1].title)
    }

    @Test
    fun testGetPostByIdReturnsNullForNonExistentId() = runBlocking {
        // Act
        val result = dao.getPostById(999L)

        // Assert
        assertNull(result)
    }
}