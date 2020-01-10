package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.TestFactories.createUserTag
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoResourceFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class VideosLinkBuilderTest {
    private lateinit var videosLinkBuilder: VideosLinkBuilder
    private val validVideoId: String = ObjectId().toHexString()

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        videosLinkBuilder = VideosLinkBuilder(mock)
    }

    @Test
    fun `self link`() {
        val link = videosLinkBuilder.self(VideoResourceFactory.sample(id = "self-test").id)

        assertThat(link.href).isEqualTo("https://localhost/v1/videos/self-test")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `interaction link`() {
        val link = videosLinkBuilder.createVideoInteractedWithEvent(VideoResourceFactory.sample(id = "video-id").id)

        assertThat(link.href).isEqualTo("https://localhost/v1/videos/video-id/events?logVideoInteraction=true&type={type}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `video link`() {
        val link = videosLinkBuilder.videoLink()

        assertThat(link.href).isEqualTo("https://localhost/v1/videos/{id}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.VIDEO)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = videosLinkBuilder.searchVideosLink()!!

        assertThat(link.href).contains("/v1/videos{?query,sort_by,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject,subjects_set_manually,promoted,content_partner,type,is_classroom}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.SEARCH_VIDEOS)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when not authenticated`() {
        val link = videosLinkBuilder.searchVideosLink()

        assertThat(link).isNull()
    }

    @Test
    fun `adminSearch link when user can search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISABLED_VIDEOS)

        val link = videosLinkBuilder.adminSearchLink()

        assertThat(link?.href).isEqualTo("/v1/videos/search")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.ADMIN_SEARCH)
        assertThat(link?.isTemplated).isFalse()
    }

    @Test
    fun `adminSearch link returns null when user can't search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        assertThat(videosLinkBuilder.adminSearchLink()).isNull()
    }

    @Test
    fun `transcript link returns a link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val videoId = aValidId()
        val link = videosLinkBuilder.transcriptLink(createVideo(videoId = videoId, transcript = "hi"))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$videoId/transcript")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.TRANSCRIPT)
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `transcript link returns null when video has no transcripts`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val link = videosLinkBuilder.transcriptLink(
            createVideo(
                videoId = aValidId(),
                transcript = null
            )
        )

        assertThat(link).isNull()
    }

    @Test
    fun `transcript link returns null when not authenticated`() {
        val link = videosLinkBuilder.transcriptLink(createVideo())

        assertThat(link).isNull()
    }

    @Test
    fun `rate link returns a link when no rating`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link =
            videosLinkBuilder.rateLink(
                createVideo(
                    videoId = validVideoId,
                    ratings = listOf(
                        UserRating(
                            rating = 3, userId = UserId(
                                "another-teacher"
                            )
                        )
                    )
                )
            )

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link returns a link when no rating by current User`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link = videosLinkBuilder.rateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link returns a link when there is a rating by the current user`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link =
            videosLinkBuilder.rateLink(
                createVideo(
                    ratings = listOf(
                        UserRating(
                            rating = 3,
                            userId = UserId("teacher@boclips.com")
                        )
                    )
                )
            )

        assertThat(link!!.href).isNotNull()
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
    }

    @Test
    fun `rate link returns null when not authenticated`() {
        setSecurityContext("anonymousUser")

        val link = videosLinkBuilder.rateLink(createVideo())

        assertThat(link).isNull()
    }

    @Test
    fun `tag link returns a link when no best for tag`() {
        setSecurityContext("teacher@boclips.com", UserRoles.TAG_VIDEOS)

        val link =
            videosLinkBuilder.tagLink(
                createVideo(
                    videoId = validVideoId,
                    tags = emptyList()
                )
            )

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId/tags")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.TAG)
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `tag link does not return a link when existing best for tag`() {
        setSecurityContext("teacher@boclips.com", UserRoles.TAG_VIDEOS)

        val link =
            videosLinkBuilder.tagLink(
                createVideo(
                    videoId = validVideoId,
                    tags = listOf(createUserTag())
                )
            )

        assertThat(link).isNull()
    }

    @Test
    fun `tag link returns null when not authenticated`() {
        val link = videosLinkBuilder.tagLink(createVideo())

        assertThat(link).isNull()
    }

    @Test
    fun `update link is null when user us not allowed`() {
        setSecurityContext("teacher@boclips.com")

        val link = videosLinkBuilder.updateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNull()
    }

    @Test
    fun `update link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.UPDATE_VIDEOS)

        val link = videosLinkBuilder.updateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull
        assertThat(link?.href).contains("/v1/videos/$validVideoId{?title,description,promoted,subjectIds}")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.UPDATE)
    }
}
