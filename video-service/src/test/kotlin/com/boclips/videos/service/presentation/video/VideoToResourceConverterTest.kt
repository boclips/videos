package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import com.boclips.videos.service.testsupport.TestFactories.createYoutubePlayback
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.Duration

internal class VideoToResourceConverterTest {

    val kalturaVideo = createVideo(
        videoAsset = createVideoAsset(
            title = "Do what you love",
            description = "Best bottle slogan",
            contentPartnerId = "WeWork",
            contentPartnerVideoId = "111",
            type = LegacyVideoType.TED_TALKS,
            subjects = setOf(Subject("Maths")),
            searchable = true,
            legalRestrictions = "None"
        ),
        videoPlayback = TestFactories.createKalturaPlayback()
    )

    val youtubeVideo = createVideo(
        videoAsset = createVideoAsset(
            title = "Do what you love on youtube",
            description = "Best bottle slogan",
            contentPartnerId = "JacekWork",
            contentPartnerVideoId = "222",
            type = LegacyVideoType.OTHER,
            subjects = setOf(Subject("Biology")),
            searchable = false,
            legalRestrictions = "Many"
        ),
        videoPlayback = createYoutubePlayback()
    )

    @Test
    fun `converts a video from AssetId`() {
        val videoId = ObjectId().toHexString()
        val videoResource = VideoToResourceConverter().wrapVideoAssetIdsInResource(listOf(AssetId(videoId))).first().content

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
        val videoResource = VideoToResourceConverter().fromVideo(kalturaVideo).content

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

        assertThat(videoResource.playback!!.type).isEqualTo("STREAM")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("kaltura-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat(videoResource.playback!!.id).isEqualTo("555")
        assertThat((videoResource.playback!! as StreamPlaybackResource).streamUrl).isEqualTo("kaltura-stream")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = VideoToResourceConverter().fromVideo(youtubeVideo).content

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentPartner).isEqualTo("JacekWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("222")
        assertThat(videoResource.subjects).containsExactly("Biology")
        assertThat(videoResource.type!!.id).isEqualTo(0)
        assertThat(videoResource.type!!.name).isEqualTo("Other")
        assertThat(videoResource.playback!!.type).isEqualTo("YOUTUBE")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("youtube-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(21))
        assertThat(videoResource.playback!!.id).isEqualTo("444")
        assertThat(videoResource.badges).isEqualTo(setOf("youtube"))
        assertThat(videoResource.status).isEqualTo(VideoResourceStatus.SEARCH_DISABLED)
        assertThat(videoResource.legalRestrictions).isEqualTo("Many")
    }

    @Test
    fun `converts heterogenous video lists`() {
        val resultResource = VideoToResourceConverter()
            .wrapVideosInResource(videos = listOf(youtubeVideo, kalturaVideo))

        assertThat(resultResource.map { it.content.playback!!.type }).containsExactlyInAnyOrder("STREAM", "YOUTUBE")
    }
}