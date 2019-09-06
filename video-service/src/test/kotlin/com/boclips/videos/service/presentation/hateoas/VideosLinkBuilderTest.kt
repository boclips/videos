package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories.createUserTag
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoResourceFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder

class VideosLinkBuilderTest {
    val builder = VideosLinkBuilder()

    val validVideoId = ObjectId().toHexString()

    @AfterEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `self link`() {
        val link = VideosLinkBuilder().self(VideoResourceFactory.sample(id = "self-test"))

        assertThat(link.href).isEqualTo("/v1/videos/self-test")
        assertThat(link.rel).isEqualTo("self")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `interaction link`() {
        val link = VideosLinkBuilder().createVideoInteractedWithEvent(VideoResourceFactory.sample(id = "video-id"))

        assertThat(link.href).isEqualTo("/v1/videos/video-id/events?logVideoInteraction=true&type={type}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `video link`() {
        val link = VideosLinkBuilder().videoLink()

        assertThat(link.href).isEqualTo("/v1/videos/{id}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.VIDEO)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = VideosLinkBuilder().searchVideosLink()!!

        assertThat(link.href).isEqualTo("/v1/videos{?query,sort_by,include_tag,exclude_tag,duration_min,duration_max,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subject}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.SEARCH_VIDEOS)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when not authenticated`() {
        val link = VideosLinkBuilder().searchVideosLink()

        assertThat(link).isNull()
    }

    @Test
    fun `videos link when update - insert role`() {
        setSecurityContext("teacher@boclips.com", UserRoles.INSERT_VIDEOS)

        val link = VideosLinkBuilder().videosLink()!!

        assertThat(link.href).isEqualTo("/v1/videos")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.VIDEOS)
        assertThat(link.isTemplated).isFalse()

        setSecurityContext("teacher@boclips.com", UserRoles.UPDATE_VIDEOS)
        assertThat(VideosLinkBuilder().videosLink()).isNotNull

        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)
        assertThat(VideosLinkBuilder().videosLink()).isNull()
    }

    @Test
    fun `videos link when authenticated`() {
        setSecurityContext("teacher@boclips.com")

        assertThat(VideosLinkBuilder().videosLink()).isNull()
    }

    @Test
    fun `videos link when not authenticated`() {
        assertThat(VideosLinkBuilder().videosLink()).isNull()
    }

    @Test
    fun `adminSearch link when user can search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_DISABLED_VIDEOS)

        val link = VideosLinkBuilder().adminSearchLink()

        assertThat(link?.href).isEqualTo("/v1/videos/search")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.ADMIN_SEARCH)
        assertThat(link?.isTemplated).isFalse()
    }

    @Test
    fun `adminSearch link returns null when user can't search disabled videos`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        assertThat(VideosLinkBuilder().adminSearchLink()).isNull()
    }

    @Test
    fun `transcript link returns a link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val link = VideosLinkBuilder().transcriptLink(VideoResourceFactory.sample(id = "transcript-test"))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/transcript-test/transcript")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.TRANSCRIPT)
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `transcript link returns null when video has no transcripts`() {
        setSecurityContext("teacher@boclips.com", UserRoles.DOWNLOAD_TRANSCRIPT)

        val link = VideosLinkBuilder().transcriptLink(
            VideoResourceFactory.sample(
                id = "transcript-test",
                hasTranscripts = false
            )
        )

        assertThat(link).isNull()
    }

    @Test
    fun `transcript link returns null when not authenticated`() {
        val link = VideosLinkBuilder().transcriptLink(VideoResourceFactory.sample(id = "transcript-test"))

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
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link returns a link when no rating by current User`() {
        setSecurityContext("teacher@boclips.com", UserRoles.RATE_VIDEOS)

        val link = builder.rateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull

        assertThat(link!!.href).isEqualTo("/v1/videos/$validVideoId?rating={rating}")
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `rate link returns a link when there is a rating by the current user`() {
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

        assertThat(link!!.href).isNotNull()
        assertThat(link.rel).isEqualTo(VideosLinkBuilder.Rels.RATE)
    }

    @Test
    fun `rate link returns null when not authenticated`() {
        val link = builder.rateLink(createVideo())

        assertThat(link).isNull()
    }

    @Test
    fun `tag link returns a link when no best for tag`() {
        setSecurityContext("teacher@boclips.com", UserRoles.TAG_VIDEOS)

        val link =
            builder.tagLink(
                createVideo(
                    videoId = validVideoId,
                    tag = null
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
            builder.tagLink(
                createVideo(
                    videoId = validVideoId,
                    tag = createUserTag()
                )
            )

        assertThat(link).isNull()
    }

    @Test
    fun `tag link returns null when not authenticated`() {
        val link = builder.tagLink(createVideo())

        assertThat(link).isNull()
    }

    @Test
    fun `update link is null when user us not allowed`() {
        setSecurityContext("teacher@boclips.com")

        val link = builder.updateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNull()
    }

    @Test
    fun `update link is there when user is allowed`() {
        setSecurityContext("boclip@boclips.com", UserRoles.UPDATE_VIDEOS)

        val link = builder.updateLink(createVideo(videoId = validVideoId))

        assertThat(link).isNotNull
        assertThat(link?.href).isEqualTo("/v1/videos/$validVideoId{?title,description}")
        assertThat(link?.rel).isEqualTo(VideosLinkBuilder.Rels.UPDATE)
    }
}
