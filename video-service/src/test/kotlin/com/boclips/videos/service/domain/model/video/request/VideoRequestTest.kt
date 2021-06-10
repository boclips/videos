package com.boclips.videos.service.domain.model.video.request

import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.VideoAccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VideoRequestTest {

    @Test
    fun `pass phrase query through`() {
        val searchQuery = VideoRequest(
            text = "some phrase",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0)
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.phrase).isEqualTo("some phrase")
    }

    @Test
    fun `pass ids through`() {
        val searchQuery = VideoRequest(
            text = "any query",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            ids = setOf("id 1", "id 2")
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.ids).isEqualTo(setOf("id 1", "id 2"))
    }

    @Test
    fun `pass channel ids through`() {
        val searchQuery = VideoRequest(
            text = "any query",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            ids = setOf("id 1", "id 2"),
            channelIds = setOf("1", "2", "3")
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.channelIds).isEqualTo(setOf("1", "2", "3"))
    }

    @Test
    fun `allows filtering by bestFor`() {
        val searchQuery = VideoRequest(
            text = "any query",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            bestFor = listOf("explainer")
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.bestFor).contains("explainer")
    }

    @Test
    fun `allows ordering of results by releaseDate descending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.RELEASE_DATE
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.DESC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::releaseDate)
    }

    @Test
    fun `allows ordering of results by title descending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.TITLE_DESC
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.DESC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::rawTitle)
    }

    @Test
    fun `allows ordering of results by empty category codes ascending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.UNTAGGED_CATEGORIES
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.ASC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::categoryCodes)
    }

    @Test
    fun `allows ordering of results by title ascending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.TITLE_ASC
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.ASC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::rawTitle)
    }

    @Test
    fun `allows ordering of results by ingest date ascending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.INGEST_ASC
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.ASC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::ingestedAt)
    }

    @Test
    fun `allows ordering of results by ingest date descending`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.INGEST_DESC
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        val sort = searchQuery.sort.first() as Sort.ByField<VideoMetadata>

        assertThat(sort.order).isEqualTo(SortOrder.DESC)
        assertThat(sort.fieldName).isEqualTo(VideoMetadata::ingestedAt)
    }

    @Test
    fun `allows ordering of results by random`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.RANDOM
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.sort.first() is Sort.ByRandom<VideoMetadata>)
    }

    @Test
    fun `does not sort the results without a sortBy`() {
        val searchQuery = VideoRequest(
            text = "id:11,12,13",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = null
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.sort).isEmpty()
    }

    @Test
    fun `allows filtering of source`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.RELEASE_DATE,
            source = SourceType.YOUTUBE
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.source).isEqualTo(SourceType.YOUTUBE)
    }

    @Test
    fun `allows filtering of content type`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            types = setOf(VideoType.NEWS, VideoType.STOCK)
        )
            .toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.types).containsExactly(VideoType.NEWS, VideoType.STOCK)
    }

    @Test
    fun `allows filtering of release date`() {
        val searchQuery = VideoRequest(
            text = "testing",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            sortBy = SortKey.RELEASE_DATE,
            releaseDateFrom = LocalDate.of(2000, 1, 1),
            releaseDateTo = LocalDate.of(2001, 1, 1)
        ).toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.releaseDateTo).isEqualTo(LocalDate.of(2001, 1, 1))
        assertThat(searchQuery.userQuery.releaseDateFrom).isEqualTo(LocalDate.of(2000, 1, 1))
    }

    @Test
    fun `allows filtering by promoted`() {
        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            promoted = true
        ).toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.promoted).isEqualTo(true)
    }

    @Test
    fun `allows filtering by attachment type`() {
        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            attachmentTypes = setOf("Activity")
        ).toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.userQuery.attachmentTypes).isEqualTo(setOf("Activity"))
    }

    @Test
    fun `does not limit ids when has access to everything`() {
        val searchQuery = VideoRequest(
            text = "",
            pageSize = 2,
            pagingState = VideoRequestPagingState.PageNumber(0),
            promoted = true
        ).toQuery(VideoAccess.Everything(emptySet()))

        assertThat(searchQuery.videoAccessRuleQuery.permittedVideoIds).isNull()
    }
}
