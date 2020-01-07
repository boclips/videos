package com.boclips.videos.service.domain.model.video

import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VideoSearchVideoQueryTest {

    @Test
    fun `translate phrase query`() {
        val searchQuery = VideoSearchQuery(
            text = "normal phrase",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery(videoAccessRule = VideoAccessRule.Everything)

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
            .toSearchQuery(VideoAccessRule.Everything)

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
            .toSearchQuery(VideoAccessRule.Everything)

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
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.includeTags).contains("classroom")
    }

    @Test
    fun `allows filtering by bestFor`() {
        val searchQuery = VideoSearchQuery(
            text = "id:3222",
            includeTags = listOf("classroom"),
            excludeTags = emptyList(),
            bestFor = listOf("explainer"),
            pageSize = 2,
            pageIndex = 0
        )
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.bestFor).contains("explainer")
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
            .toSearchQuery(VideoAccessRule.Everything)

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
            .toSearchQuery(VideoAccessRule.Everything)

        val sort = searchQuery.sort as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.DESC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::releaseDate)
    }

    @Test
    fun `allows ordering of results by random`() {
        val searchQuery = VideoSearchQuery(
            text = "testing",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RANDOM
        )
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.sort is Sort.ByRandom<VideoMetadata>)
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
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.sort).isNull()
    }

    @Test
    fun `allows filtering of source`() {
        val searchQuery = VideoSearchQuery(
            text = "testing",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE,
            source = SourceType.YOUTUBE
        )
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.source).isEqualTo(SourceType.YOUTUBE)
    }

    @Test
    fun `allows filtering of content type`() {
        val searchQuery = VideoSearchQuery(
            text = "testing",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            type = setOf(VideoType.NEWS, VideoType.STOCK)
        )
            .toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.type).containsExactly(VideoType.NEWS, VideoType.STOCK)
    }

    @Test
    fun `allows filtering of release date`() {
        val searchQuery = VideoSearchQuery(
            text = "testing",
            includeTags = emptyList(),
            excludeTags = listOf("classroom"),
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE,
            releaseDateFrom = LocalDate.of(2000, 1, 1),
            releaseDateTo = LocalDate.of(2001, 1, 1)
        ).toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.releaseDateTo).isEqualTo(LocalDate.of(2001, 1, 1))
        assertThat(searchQuery.releaseDateFrom).isEqualTo(LocalDate.of(2000, 1, 1))
    }

    @Test
    fun `allows filtering by promoted`() {
        val searchQuery = VideoSearchQuery(
            text = "",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.promoted).isEqualTo(true)
    }

    @Test
    fun `limits query to specific ids`() {
        val firstId = TestFactories.createVideoId()
        val secondId = TestFactories.createVideoId()

        val searchQuery = VideoSearchQuery(
            text = "",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toSearchQuery(VideoAccessRule.SpecificIds(setOf(firstId, secondId)))

        assertThat(searchQuery.permittedVideoIds).containsExactlyInAnyOrder(firstId.value, secondId.value)
    }

    @Test
    fun `does not limit ids when has access to everything`() {
        val searchQuery = VideoSearchQuery(
            text = "",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toSearchQuery(VideoAccessRule.Everything)

        assertThat(searchQuery.permittedVideoIds).isNull()
    }
}
