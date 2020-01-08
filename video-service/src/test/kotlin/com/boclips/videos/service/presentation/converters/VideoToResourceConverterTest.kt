package com.boclips.videos.service.presentation.converters

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

internal class VideoToResourceConverterTest {
    private lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    private lateinit var videosLinkBuilder: VideosLinkBuilder
    private lateinit var videoToResourceConverter: VideoToResourceConverter

    private val kalturaVideo = createVideo(
        title = "Do what you love",
        description = "Best bottle slogan",
        contentPartnerName = "WeWork",
        contentPartnerVideoId = "111",
        type = ContentType.NEWS,
        subjects = setOf(TestFactories.createSubject(id = "maths-subject-id", name = "Maths")),
        legalRestrictions = "None",
        ageRange = AgeRange.bounded(min = 5, max = 11),
        ratings = listOf(
            UserRating(
                rating = 3, userId = UserId(
                    "user-id"
                )
            )
        ),
        tag = TestFactories.createUserTag("tag-id", "tag-label", "user-id"),
        promoted = true
    )

    private val youtubeVideo = createVideo(
        title = "Do what you love on youtube",
        description = "Best bottle slogan",
        contentPartnerName = "JacekWork",
        contentPartnerVideoId = "222",
        playback = TestFactories.createYoutubePlayback(),
        type = ContentType.INSTRUCTIONAL_CLIPS,
        subjects = setOf(TestFactories.createSubject(id = "biology-subject-id", name = "Biology")),
        legalRestrictions = "Many",
        tag = TestFactories.createUserTag("tag-id", "tag-label", "user-id")
    )

    @BeforeEach
    fun setUp() {
        videosLinkBuilder = mock()
        playbackToResourceConverter =
            PlaybackToResourceConverter(
                mock(),
                PlaybacksLinkBuilder(TestKalturaClient())
            )
        videoToResourceConverter =
            VideoToResourceConverter(
                videosLinkBuilder,
                playbackToResourceConverter
            )
    }

    @Test
    fun `converts a video from AssetId`() {
        val videoId = ObjectId().toHexString()
        val videoResource =
            videoToResourceConverter.convertVideoIds(listOf(VideoId(videoId))).first()

        assertThat(videoResource.id).isEqualTo(videoId)
        assertThat(videoResource.title).isNull()
        assertThat(videoResource.description).isNull()
        assertThat(videoResource.createdBy).isEqualTo(videoResource.contentPartner)
        assertThat(videoResource.contentPartner).isNull()
        assertThat(videoResource.contentPartnerVideoId).isNull()
        assertThat(videoResource.subjects).isNullOrEmpty()
        assertThat(videoResource.type).isNull()
        assertThat(videoResource.playback).isNull()
        assertThat(videoResource.badges).isNullOrEmpty()
    }

    @Test
    fun `converts a video from Kaltura`() {
        val videoResource = videoToResourceConverter.convertVideo(kalturaVideo, UserFactory.sample(id = "user-id"))

        assertThat(videoResource.title).isEqualTo("Do what you love")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.createdBy).isEqualTo(videoResource.contentPartner)
        assertThat(videoResource.contentPartner).isEqualTo("WeWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("111")
        assertThat(videoResource.subjects).containsExactly(
            SubjectResource(
                id = "maths-subject-id",
                name = "Maths"
            )
        )
        assertThat(videoResource.type!!.id).isEqualTo(1)
        assertThat(videoResource.type!!.name).isEqualTo("News")
        assertThat(videoResource.badges).isEqualTo(setOf("ad-free"))
        assertThat(videoResource.legalRestrictions).isEqualTo("None")

        assertThat(videoResource.ageRange!!.min).isEqualTo(5)
        assertThat(videoResource.ageRange!!.max).isEqualTo(11)
        assertThat(videoResource.rating).isEqualTo(3.0)
        assertThat(videoResource.yourRating).isEqualTo(3.0)
        assertThat(videoResource.bestFor!!.map { it.label }).containsOnly("tag-label")
        assertThat(videoResource.promoted).isEqualTo(true)

        val playbackResource = videoResource.playback!! as StreamPlaybackResource
        assertThat(playbackResource.type).isEqualTo("STREAM")
        assertThat(playbackResource.thumbnailUrl).isEqualTo("https://cdnapisec.kaltura.com/p/partner-id/thumbnail/entry_id/entry-id/width/500/vid_slices/3/vid_slice/1")
        assertThat(playbackResource.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat(playbackResource.id).isEqualTo("entry-id")
        assertThat(playbackResource.streamUrl).contains("applehttp")
        assertThat(playbackResource.referenceId).isEqualTo("555")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = videoToResourceConverter.convertVideo(youtubeVideo, UserFactory.sample())

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.createdBy).isEqualTo(videoResource.contentPartner)
        assertThat(videoResource.contentPartner).isEqualTo("JacekWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("222")
        assertThat(videoResource.subjects).containsExactly(
            SubjectResource(
                id = "biology-subject-id",
                name = "Biology"
            )
        )
        assertThat(videoResource.type!!.id).isEqualTo(3)
        assertThat(videoResource.type!!.name).isEqualTo("Instructional Clips")
        assertThat((videoResource.playback!! as YoutubePlaybackResource).type).isEqualTo("YOUTUBE")
        assertThat((videoResource.playback!! as YoutubePlaybackResource).thumbnailUrl).isEqualTo("youtube-thumbnail")
        assertThat((videoResource.playback!! as YoutubePlaybackResource).duration).isEqualTo(Duration.ofSeconds(21))
        assertThat((videoResource.playback!! as YoutubePlaybackResource).id).isEqualTo("444")
        assertThat(videoResource.badges).isEqualTo(setOf("youtube"))
        assertThat(videoResource.legalRestrictions).isEqualTo("Many")
        assertThat(videoResource.bestFor!!.map { it.label }).containsOnly("tag-label")
    }

    @Test
    fun `converts bestFor to empty list when video does not have a tag`() {
        val video = createVideo(
            title = "Do what you love on youtube",
            description = "Best bottle slogan",
            contentPartnerName = "JacekWork",
            contentPartnerVideoId = "222",
            playback = TestFactories.createYoutubePlayback(),
            type = ContentType.INSTRUCTIONAL_CLIPS,
            subjects = setOf(TestFactories.createSubject(id = "biology-subject-id", name = "Biology")),
            legalRestrictions = "Many",
            tag = null
        )

        val videoResource = videoToResourceConverter.convertVideo(video, UserFactory.sample())

        assertThat(videoResource.bestFor).isEmpty()
    }

    @Test
    fun `converts heterogenous video lists`() {
        val resultResource = videoToResourceConverter
            .convertVideos(videos = listOf(youtubeVideo, kalturaVideo), user = UserFactory.sample())

        assertThat(resultResource.map { it.playback!!.type }).containsExactlyInAnyOrder(
            "STREAM",
            "YOUTUBE"
        )
    }
}
