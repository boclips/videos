package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories
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

    val uriComponentsBuilderMock = mock<UriComponentsBuilderFactory>()

    @BeforeEach
    fun setUp() {
        whenever(uriComponentsBuilderMock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        videosLinkBuilder = VideosLinkBuilder(uriComponentsBuilderMock)
    }

    @Test
    fun `video resource self link`() {
        val link = videosLinkBuilder.self(VideoResourceFactory.sample(id = "self-test").id)

        assertThat(link.href).endsWith("/v1/videos/self-test")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `video resource self with full projection`() {
        whenever(uriComponentsBuilderMock.getInstance()).thenReturn(
            UriComponentsBuilder.fromHttpUrl("https://localhost/v1/videos/123?projection=full")
        )

        val link = videosLinkBuilder.self(VideoResourceFactory.sample(id = "self-test").id)

        assertThat(link.href).endsWith("/v1/videos/self-test?projection=full")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `video resource self link ignores params when requesting videos`() {
        whenever(uriComponentsBuilderMock.getInstance()).thenReturn(
            UriComponentsBuilder.fromHttpUrl("https://localhost/v1/videos?query=tom")
        )

        val link = videosLinkBuilder.self(VideoResourceFactory.sample(id = "self-test").id)

        assertThat(link.href).endsWith("/v1/videos/self-test")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `video resource self with details projection`() {
        whenever(uriComponentsBuilderMock.getInstance()).thenReturn(
            UriComponentsBuilder.fromHttpUrl("https://localhost/v1/videos/123?projection=details")
        )

        val link = videosLinkBuilder.self(VideoResourceFactory.sample(id = "self-test").id)

        assertThat(link.href).endsWith("/v1/videos/self-test?projection=details")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `details projection link`() {
        setSecurityContext("teacher@boclips.com", UserRoles.BACKOFFICE)

        val link = videosLinkBuilder.videoDetailsProjection(VideoResourceFactory.sample(id = "self-test").id)!!

        assertThat(link.href).endsWith("/v1/videos/self-test?projection=details")
        assertThat(link.rel).isEqualTo("detailsProjection")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `full projection link`() {
        setSecurityContext("teacher@boclips.com", UserRoles.BACKOFFICE)

        val link = videosLinkBuilder.videoFullProjection(VideoResourceFactory.sample(id = "self-test").id)!!

        assertThat(link.href).endsWith("/v1/videos/self-test?projection=full")
        assertThat(link.rel).isEqualTo("fullProjection")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `interaction link`() {
        val link = videosLinkBuilder.createVideoInteractedWithEvent(VideoResourceFactory.sample(id = "video-id").id)

        assertThat(link.href).endsWith("/v1/videos/video-id/events?logVideoInteraction=true&type={type}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION)
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `video link`() {
        val link = videosLinkBuilder.videoLink()

        assertThat(link.href).endsWith("/v1/videos/{id}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.VIDEO)
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `search link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = videosLinkBuilder.searchVideosLink()!!

        assertThat(link.href).contains("/v1/videos{?query,id,sort_by,duration,duration_facets,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,age_range,age_range_facets,size,page,subject,subjects_set_manually,promoted,content_partner,type,resource_types,resource_type_facets}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.SEARCH_VIDEOS)
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `search link with query prepoulated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = videosLinkBuilder.searchVideosByText("search")!!

        assertThat(link.href).contains("/v1/videos?query=search{&id,sort_by,duration,duration_facets,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,age_range,age_range_facets,size,page,subject,subjects_set_manually,promoted,content_partner,type,resource_types,resource_type_facets}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.SEARCH_VIDEOS)
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `search link when not authenticated`() {
        val link = videosLinkBuilder.searchVideosLink()

        assertThat(link).isNull()
    }

    @Test
    fun `transcript link returns a link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val videoId = aValidId()
        val link = videosLinkBuilder.transcriptLink(createVideo(videoId = videoId, transcript = "hi"))

        assertThat(link).isNotNull

        assertThat(link!!.href).endsWith("/v1/videos/$videoId/transcript")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.TRANSCRIPT)
        assertThat(link.templated).isFalse()
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
                            rating = 3, userId = UserId("another-teacher")
                        )
                    )
                )
            )

        assertThat(link).isNotNull

        assertThat(link!!.href).contains("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `rate link returns a link when no rating by current User`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link = videosLinkBuilder.rateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull

        assertThat(link!!.href).contains("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.templated).isTrue()
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

        assertThat(link!!.href).endsWith("/v1/videos/$validVideoId/tags")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.TAG)
        assertThat(link.templated).isFalse()
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
        assertThat(link?.href).contains("/v1/videos/$validVideoId")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.UPDATE)
    }

    @Test
    fun `update captions link is null when user is not allowed`() {
        setSecurityContext("teacher@boclips.com")

        val link = videosLinkBuilder.updateCaptions(createVideo(videoId = validVideoId))

        assertThat(link).isNull()
    }

    @Test
    fun `update captions link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.UPDATE_VIDEOS)

        val link = videosLinkBuilder.updateCaptions(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull
        assertThat(link?.href).contains("/v1/videos/$validVideoId/captions")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.UPDATE_CAPTIONS)
    }

    @Test
    fun `get captions link is null when user is not allowed`() {
        setSecurityContext("teacher@boclips.com")

        val link = videosLinkBuilder.getCaptions()

        assertThat(link).isNull()
    }

    @Test
    fun `get captions link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.UPDATE_VIDEOS)

        val link = videosLinkBuilder.getCaptions()

        assertThat(link).isNotNull
        assertThat(link?.href).contains("/v1/videos/{id}/captions")
        assertThat(link?.templated).isTrue()
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.GET_CAPTIONS)
    }

    @Test
    fun `add attachment link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.UPDATE_VIDEOS)

        val link = videosLinkBuilder.addAttachment(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull
        assertThat(link?.href).contains("/v1/videos/$validVideoId/attachments")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.ADD_ATTACHMENT)
    }

    @Test
    fun `assets link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.DOWNLOAD_VIDEO)

        val link = videosLinkBuilder.assets(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull
        assertThat(link?.href).contains("/v1/videos/$validVideoId/assets")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.ASSETS)
    }

    @Test
    fun `assets link is not present with incorrect permission`() {
        setSecurityContext("boclip@boclips.com")

        val link = videosLinkBuilder.assets(createVideo(videoId = validVideoId))

        assertThat(link).isNull()
    }

    @Test
    fun `assets link is not present for a youtube video`() {
        setSecurityContext("boclip@boclips.com")

        val link = videosLinkBuilder.assets(
            createVideo(
                videoId = validVideoId,
                playback = TestFactories.createYoutubePlayback()
            )
        )

        assertThat(link).isNull()
    }
}
