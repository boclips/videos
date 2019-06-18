package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.VideoResourceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder

class VideosLinkBuilderTest {

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
    fun `video link`() {
        val link = VideosLinkBuilder().videoLink()

        assertThat(link.href).isEqualTo("/v1/videos/{id}")
        assertThat(link.rel).isEqualTo("video")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `search link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = VideosLinkBuilder().searchVideosLink()!!

        assertThat(link.href).isEqualTo("/v1/videos{?query,sort_by,include_tag,exclude_tag,min_duration,max_duration,released_date_from,released_date_to,source,age_range_min,age_range_max,size,page,subjects}")
        assertThat(link.rel).isEqualTo("searchVideos")
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
        assertThat(link.rel).isEqualTo("videos")
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
        assertThat(link?.rel).isEqualTo("adminSearch")
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
        assertThat(link.rel).isEqualTo("transcript")
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
}
