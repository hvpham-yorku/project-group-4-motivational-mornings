package com.example.motivationalmornings

import com.example.motivationalmornings.Persistence.RssRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RssRepositoryTest {

    private lateinit var repository: RssRepository

    @Before
    fun setup() {
        repository = RssRepository()
    }

    @Test
    fun getRssItems_blankUrl_returnsEmptyList() {
        val items = repository.getRssItems(" ")
        assertTrue(items.isEmpty())
    }

    @Test
    fun getRssItems_invalidUrl_returnsEmptyList() {
        val items = repository.getRssItems("not a url")
        assertTrue(items.isEmpty())
    }
}
