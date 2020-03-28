package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VideoRequestTest {

    @Test
    fun `translate phrase query`() {
        val searchQuery = VideoRequest(
            text = "normal phrase",
            pageSize = 2,
            pageIndex = 0
        )
            .toQuery(videoAccess = VideoAccess.Everything)

        assertThat(searchQuery.phrase).isEqualTo("normal phrase")
    }

    @Test
    fun `translate single id query`() {
        val searchQuery = VideoRequest(
            text = "id:11",
            pageSize = 2,
            pageIndex = 0
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.ids).containsExactly("11")
    }

    @Test
    fun `translate multiple id query`() {
        val searchQuery = VideoRequest(
            text = "id:11,12,13",
            pageSize = 2,
            pageIndex = 0
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.ids).containsExactly("11", "12", "13")
    }

    @Test
    fun `allows filtering by bestFor`() {
        val searchQuery = VideoRequest(
            text = "id:3222",
            bestFor = listOf("explainer"),
            pageSize = 2,
            pageIndex = 0
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.bestFor).contains("explainer")
    }

    @Test
    fun `allows ordering of results by releaseDate descending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE
        )
            .toQuery(VideoAccess.Everything)

        val sort = searchQuery.sort as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.DESC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::releaseDate)
    }

    @Test
    fun `allows ordering of results by random`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RANDOM
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.sort is Sort.ByRandom<VideoMetadata>)
    }

    @Test
    fun `does not sort the results without a sortBy`() {
        val searchQuery = VideoRequest(
            text = "id:11,12,13",
            pageSize = 2,
            pageIndex = 0,
            sortBy = null
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.sort).isNull()
    }

    @Test
    fun `allows filtering of source`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE,
            source = SourceType.YOUTUBE
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.source).isEqualTo(SourceType.YOUTUBE)
    }

    @Test
    fun `allows filtering of content type`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pageIndex = 0,
            type = setOf(VideoType.NEWS, VideoType.STOCK)
        )
            .toQuery(VideoAccess.Everything)

        assertThat(searchQuery.includedType).containsExactly(VideoType.NEWS, VideoType.STOCK)
    }

    @Test
    fun `allows filtering of release date`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pageIndex = 0,
            sortBy = SortKey.RELEASE_DATE,
            releaseDateFrom = LocalDate.of(2000, 1, 1),
            releaseDateTo = LocalDate.of(2001, 1, 1)
        ).toQuery(VideoAccess.Everything)

        assertThat(searchQuery.releaseDateTo).isEqualTo(LocalDate.of(2001, 1, 1))
        assertThat(searchQuery.releaseDateFrom).isEqualTo(LocalDate.of(2000, 1, 1))
    }

    @Test
    fun `allows filtering by promoted`() {
        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toQuery(VideoAccess.Everything)

        assertThat(searchQuery.promoted).isEqualTo(true)
    }

    @Test
    fun `limits query to specific ids`() {
        val firstId = TestFactories.createVideoId()
        val secondId = TestFactories.createVideoId()

        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toQuery(
            VideoAccess.Rules(
                accessRules = listOf(
                    VideoAccessRule.IncludedIds(
                        videoIds = setOf(
                            firstId,
                            secondId
                        )
                    )
                )
            )
        )

        assertThat(searchQuery.permittedVideoIds).containsExactlyInAnyOrder(firstId.value, secondId.value)
    }

    @Test
    fun `does not limit ids when has access to everything`() {
        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pageIndex = 0,
            promoted = true
        ).toQuery(VideoAccess.Everything)

        assertThat(searchQuery.permittedVideoIds).isNull()
    }
}
