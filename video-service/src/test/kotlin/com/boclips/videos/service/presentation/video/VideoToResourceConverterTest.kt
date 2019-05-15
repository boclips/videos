package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

internal class VideoToResourceConverterTest {

    private lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    private lateinit var videosLinkBuilder: VideosLinkBuilder
    private lateinit var videoToResourceConverter: VideoToResourceConverter

    @BeforeEach
    fun setUp() {
        videosLinkBuilder = mock()
        playbackToResourceConverter = PlaybackToResourceConverter(mock())
        videoToResourceConverter = VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter)
    }

    val kalturaVideo = createVideo(
        title = "Do what you love",
        description = "Best bottle slogan",
        contentPartnerId = "WeWork",
        contentPartnerVideoId = "111",
        type = LegacyVideoType.TED_TALKS,
        subjects = setOf(LegacySubject("Maths")),
        searchable = true,
        legalRestrictions = "None"
    )

    val youtubeVideo = createVideo(
        title = "Do what you love on youtube",
        description = "Best bottle slogan",
        contentPartnerId = "JacekWork",
        contentPartnerVideoId = "222",
        type = LegacyVideoType.OTHER,
        subjects = setOf(LegacySubject("Biology")),
        searchable = false,
        legalRestrictions = "Many",
        playback = TestFactories.createYoutubePlayback()
    )

    @Test
    fun `converts a video from AssetId`() {
        val videoId = ObjectId().toHexString()
        val videoResource =
            videoToResourceConverter.wrapVideoIdsInResource(listOf(VideoId(videoId))).first().content

        assertThat(videoResource.id).isEqualTo(videoId)
        assertThat(videoResource.title).isNull()
        assertThat(videoResource.description).isNull()
        assertThat(videoResource.contentPartner).isNull()
        assertThat(videoResource.contentPartnerVideoId).isNull()
        assertThat(videoResource.subjects).isNullOrEmpty()
        assertThat(videoResource.type).isNull()
        assertThat(videoResource.playback).isNull()
        assertThat(videoResource.badges).isNullOrEmpty()
        assertThat(videoResource.status).isNull()
    }

    @Test
    fun `converts a video from Kaltura`() {
        val videoResource = videoToResourceConverter.fromVideo(kalturaVideo).content

        assertThat(videoResource.title).isEqualTo("Do what you love")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentPartner).isEqualTo("WeWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("111")
        assertThat(videoResource.subjects).containsExactly("Maths")
        assertThat(videoResource.type!!.id).isEqualTo(10)
        assertThat(videoResource.type!!.name).isEqualTo("TED Talks")
        assertThat(videoResource.badges).isEqualTo(setOf("ad-free"))
        assertThat(videoResource.status).isEqualTo(VideoResourceStatus.SEARCHABLE)
        assertThat(videoResource.legalRestrictions).isEqualTo("None")

        assertThat(videoResource.playback!!.content.type).isEqualTo("STREAM")
        assertThat(videoResource.playback!!.content.thumbnailUrl).isEqualTo("kaltura-thumbnailUrl")
        assertThat(videoResource.playback!!.content.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat(videoResource.playback!!.content.id).isEqualTo("555")
        assertThat((videoResource.playback!!.content as StreamPlaybackResource).streamUrl).isEqualTo("hls-stream")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = videoToResourceConverter.fromVideo(youtubeVideo).content

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentPartner).isEqualTo("JacekWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("222")
        assertThat(videoResource.subjects).containsExactly("Biology")
        assertThat(videoResource.type!!.id).isEqualTo(0)
        assertThat(videoResource.type!!.name).isEqualTo("Other")
        assertThat(videoResource.playback!!.content.type).isEqualTo("YOUTUBE")
        assertThat(videoResource.playback!!.content.thumbnailUrl).isEqualTo("youtube-thumbnail")
        assertThat(videoResource.playback!!.content.duration).isEqualTo(Duration.ofSeconds(21))
        assertThat(videoResource.playback!!.content.id).isEqualTo("444")
        assertThat(videoResource.badges).isEqualTo(setOf("youtube"))
        assertThat(videoResource.status).isEqualTo(VideoResourceStatus.SEARCH_DISABLED)
        assertThat(videoResource.legalRestrictions).isEqualTo("Many")
    }

    @Test
    fun `converts heterogenous video lists`() {
        val resultResource = videoToResourceConverter
            .wrapVideosInResource(videos = listOf(youtubeVideo, kalturaVideo))

        assertThat(resultResource.map { it.content.playback!!.content.type }).containsExactlyInAnyOrder("STREAM", "YOUTUBE")
    }
}