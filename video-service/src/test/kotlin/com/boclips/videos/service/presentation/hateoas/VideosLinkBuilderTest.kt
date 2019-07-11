package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoResourceFactory
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.util.UriComponentsBuilder

class VideosLinkBuilderTest {

    val validVideoId = ObjectId().toHexString()

    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    lateinit var builder: VideosLinkBuilder

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        builder = VideosLinkBuilder(mock)
    }

    @Test
    fun `self link`() {
        val link = builder.self(VideoResourceFactory.sample(id = "self-test"))

        assertThat(link.href).isEqualTo("/v1/videos/self-test")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `video link`() {
        val link = builder.videoLink()

        assertThat(link.href).isEqualTo("/v1/videos/{id}")
        assertThat(link.rel).isEqualTo("video")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = builder.searchVideosLink()!!

        assertThat(link.href).isEqualTo("/v1/videos{?query,sort_by,include_tag,exclude_tag,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject}")
        assertThat(link.rel).isEqualTo("searchVideos")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when caller has VIEW_ANY_VIDEO role`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS, UserRoles.VIEW_ANY_VIDEO)

        val link = builder.searchVideosLink()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/videos{?query,sort_by,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject}")
        assertThat(link.rel).isEqualTo("searchVideos")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when not authenticated`() {
        val link = builder.searchVideosLink()

        assertThat(link).isNull()
    }

    @Test
    fun `videos link when update - insert role`() {
        setSecurityContext("teacher@boclips.com", UserRoles.INSERT_VIDEOS)

        val link = builder.videosLink()!!

        assertThat(link.href).isEqualTo("/v1/videos")
        assertThat(link.rel).isEqualTo("videos")
        assertThat(link.isTemplated).isFalse()

        setSecurityContext("teacher@boclips.com", UserRoles.UPDATE_VIDEOS)
        assertThat(builder.videosLink()).isNotNull

        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)
        assertThat(builder.videosLink()).isNull()
    }

    @Test
    fun `videos link when authenticated`() {
        setSecurityContext("teacher@boclips.com")

        assertThat(builder.videosLink()).isNull()
    }

    @Test
    fun `videos link when not authenticated`() {
        assertThat(builder.videosLink()).isNull()
    }

    @Test
    fun `adminSearch link when user can search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISABLED_VIDEOS)

        val link = builder.adminSearchLink()

        assertThat(link?.href).isEqualTo("/v1/videos/search")
        assertThat(link?.rel).isEqualTo("adminSearch")
        assertThat(link?.isTemplated).isFalse()
    }

    @Test
    fun `adminSearch link returns null when user can't search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        assertThat(builder.adminSearchLink()).isNull()
    }

    @Test
    fun `transcript link returns a link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val link = builder.transcriptLink(VideoResourceFactory.sample(id = "transcript-test"))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/transcript-test/transcript")
        assertThat(link.rel).isEqualTo("transcript")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `transcript link returns null when video has no transcripts`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val link = builder.transcriptLink(
            VideoResourceFactory.sample(
                id = "transcript-test",
                hasTranscripts = false
            )
        )

        assertThat(link).isNull()
    }

    @Test
    fun `transcript link returns null when not authenticated`() {
        val link = builder.transcriptLink(VideoResourceFactory.sample(id = "transcript-test"))

        assertThat(link).isNull()
    }

    @Test
    fun `rate link returns a link when no rating`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link =
            builder.rateLink(
                createVideo(
                    videoId = validVideoId,
                    ratings = listOf(UserRating(rating = 3, userId = UserId("another-teacher")))
                )
            )

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo("rate")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link returns a link when no rating by current User`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link = builder.rateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo("rate")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link does not return a link when there is a rating by the current user`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link =
            builder.rateLink(
                createVideo(
                    ratings = listOf(
                        UserRating(
                            rating = 3,
                            userId = UserId("teacher@boclips.com")
                        )
                    )
                )
            )

        assertThat(link).isNull()
    }

    @Test
    fun `rate link returns null when not authenticated`() {
        val link = builder.rateLink(createVideo())

        assertThat(link).isNull()
    }
}
