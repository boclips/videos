package com.boclips.videos.service.domain.model

import com.boclips.search.service.domain.SortOrder
import com.boclips.search.service.domain.VideoMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoSearchQueryTest {

    @Test
    fun `translate phrase query`() {
        val searchQuery = VideoSearchQuery(
            text = "normal phrase",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery()

        assertThat(searchQuery.phrase).isEqualTo("normal phrase")
    }

    @Test
    fun `translate single id query`() {
        val searchQuery = VideoSearchQuery(
            text = "id:11",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11")
    }

    @Test
    fun `translate multiple id query`() {
        val searchQuery = VideoSearchQuery(
            text = "id:11,12,13",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery()

        assertThat(searchQuery.ids).containsExactly("11", "12", "13")
    }

    @Test
    fun `allows filtering by presence of tag`() {
        val searchQuery = VideoSearchQuery(
            text = "id:11,12,13",
            includeTags = listOf("classroom"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery()

        assertThat(searchQuery.includeTags).contains("classroom")
    }

    @Test
    fun `allows filtering by absence of tag`() {
        val searchQuery = VideoSearchQuery(
            text = "id:11,12,13",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery()

        assertThat(searchQuery.excludeTags).contains("classroom")
    }

    @Test
    fun `allows ordering of results by releaseDate descending`() {
        val searchQuery = VideoSearchQuery(
            text = "testing",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE
        )
            .toSearchQuery()

        assertThat(searchQuery.sort!!.order).isEqualTo(SortOrder.DESC)
        assertThat(searchQuery.sort!!.fieldName).isEqualTo(VideoMetadata::releaseDate)
    }

    @Test
    fun `does not sort the results without a sortBy`() {
        val searchQuery = VideoSearchQuery(
            text = "id:11,12,13",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            sortBy = null
        )
            .toSearchQuery()

        assertThat(searchQuery.sort).isNull()
    }
}
