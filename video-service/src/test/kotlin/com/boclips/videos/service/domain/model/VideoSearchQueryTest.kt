package com.boclips.videos.service.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoSearchQueryTest {

    @Test
    fun `translate phrase query`() {
        val searchQuery = VideoSearchQuery(text = "normal phrase", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 2, pageIndex = 0)
                .toSearchQuery()

        assertThat(searchQuery.phrase).isEqualTo("normal phrase")
    }

    @Test
    fun `translate single id query`() {
        val searchQuery = VideoSearchQuery(text = "id:11", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 2, pageIndex = 0)
                .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11")
    }

    @Test
    fun `translate multiple id query`() {
        val searchQuery = VideoSearchQuery(text = "id:11,12,13", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 2, pageIndex = 0)
                .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11", "12", "13")
    }

    @Test
    fun `allows filtering by presence of tag`() {
        val searchQuery = VideoSearchQuery(text = "id:11,12,13", includeTags = listOf("classroom"), excludeTags = emptyList(), pageSize = 2, pageIndex = 0)
                .toSearchQuery()

        assertThat(searchQuery.includeTags).contains("classroom")
    }

    @Test
    fun `allows filtering by absence of tag`() {
        val searchQuery = VideoSearchQuery(text = "id:11,12,13", includeTags = emptyList(), excludeTags = listOf("classroom"), pageSize = 2, pageIndex = 0)
                .toSearchQuery()

        assertThat(searchQuery.excludeTags).contains("classroom")
    }
}