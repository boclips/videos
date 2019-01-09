package com.boclips.videos.service.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoSearchQueryTest {

    @Test
    fun `translate phrase query`() {
        val searchQuery = VideoSearchQuery(text = "normal phrase", filters = emptyList(), pageIndex = 0, pageSize = 2)
                .toSearchQuery()

        assertThat(searchQuery.phrase).isEqualTo("normal phrase")
    }

    @Test
    fun `translate single id query`() {
        val searchQuery = VideoSearchQuery(text = "id:11", filters = emptyList(), pageIndex = 0, pageSize = 2)
                .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11")
    }

    @Test
    fun `translate multiple id query`() {
        val searchQuery = VideoSearchQuery(text = "id:11,12,13", filters = emptyList(), pageIndex = 0, pageSize = 2)
                .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11", "12", "13")
    }

    @Test
    fun `translate educational filters`() {
        val searchQuery = VideoSearchQuery(text = "id:11,12,13", filters = listOf(VideoSearchQueryFilter.EDUCATIONAL), pageIndex = 0, pageSize = 2)
                .toSearchQuery()

        assertThat(searchQuery.includeTags).contains("classroom")
    }
}