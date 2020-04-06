package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Dimensions
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EventConverterTest {
    private val converter = EventConverter()

    @Test
    fun `creates a video event object`() {
        val id = TestFactories.aValidId()
        val video = createVideo(
            videoId = id,
            title = "the title",
            contentPartnerName = "the content partner",
            playback = TestFactories.createKalturaPlayback(
                duration = Duration.ofMinutes(2),
                originalDimensions = Dimensions(1920, 1080),
                assets = setOf(VideoFactory.createVideoAsset())
            ),
            subjects = setOf(TestFactories.createSubject(name = "physics")),
            type = ContentType.INSTRUCTIONAL_CLIPS,
            ingestedAt = ZonedDateTime.of(2020, 11, 12, 13, 14, 15, 160000000, ZoneOffset.UTC),
            ageRange = AgeRange.of(5, 10)
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.id.value).isEqualTo(id)
        assertThat(videoEvent.title).isEqualTo("the title")
        assertThat(videoEvent.contentPartner.name).isEqualTo("the content partner")
        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(videoEvent.subjects).hasSize(1)
        assertThat(videoEvent.subjects.first().name).isEqualTo("physics")
        assertThat(videoEvent.ageRange.min).isEqualTo(5)
        assertThat(videoEvent.ageRange.max).isEqualTo(10)
        assertThat(videoEvent.durationSeconds).isEqualTo(120)
        assertThat(videoEvent.type).isEqualTo(VideoType.INSTRUCTIONAL)
        assertThat(videoEvent.ingestedAt).isEqualTo("2020-11-12T13:14:15.16Z")
        assertThat(videoEvent.assets).hasSize(1)
        assertThat(videoEvent.originalDimensions.width).isEqualTo(1920)
        assertThat(videoEvent.originalDimensions.height).isEqualTo(1080)
    }

    @Test
    fun `creates a video event object when original dimensions are not known`() {
        val id = TestFactories.aValidId()
        val video = createVideo(
            videoId = id,
            playback = TestFactories.createKalturaPlayback(
                originalDimensions = null
            )
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.originalDimensions).isNull()
    }

    @Test
    fun `creates a video event object as stream playback without assets`() {
        val id = TestFactories.aValidId()
        val video = createVideo(
            videoId = id,
            playback = TestFactories.createKalturaPlayback(
                assets = null
            )
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.assets).isNull()
    }

    @Test
    fun `sets correct video type`() {
        val newsVideoEvent = converter.toVideoPayload(createVideo(type = ContentType.NEWS))
        val stockVideoEvent = converter.toVideoPayload(createVideo(type = ContentType.STOCK))
        val instructionalVideoEvent = converter.toVideoPayload(createVideo(type = ContentType.INSTRUCTIONAL_CLIPS))

        assertThat(newsVideoEvent.type).isEqualTo(VideoType.NEWS)
        assertThat(stockVideoEvent.type).isEqualTo(VideoType.STOCK)
        assertThat(instructionalVideoEvent.type).isEqualTo(VideoType.INSTRUCTIONAL)
    }

    @Test
    fun `sets correct playback provider type when YouTube`() {
        val video = createVideo(
            playback = TestFactories.createYoutubePlayback()
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.YOUTUBE)
    }

    @Test
    fun `creates a collection event object`() {
        val id = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()
        val publicCollection = TestFactories.createCollection(
            id = CollectionId(id),
            title = "collection title",
            isPublic = true,
            videos = listOf(VideoId(videoId)),
            subjects = setOf(TestFactories.createSubject(id = "subject-id", name = "subject name")),
            owner = "user-id",
            ageRangeMin = 0,
            ageRangeMax = 23,
            bookmarks = setOf(UserId("bookmarked-user-id")),
            createdAt = ZonedDateTime.of(2017, 11, 10, 1, 2, 3, 0, ZoneOffset.UTC),
            updatedAt = ZonedDateTime.of(2018, 11, 10, 1, 2, 3, 0, ZoneOffset.UTC)
        )
        val privateCollection = TestFactories.createCollection(isPublic = false)

        val publicCollectionEvent = converter.toCollectionPayload(publicCollection)
        val privateCollectionEvent = converter.toCollectionPayload(privateCollection)

        assertThat(publicCollectionEvent.id.value).isEqualTo(id)
        assertThat(publicCollectionEvent.title).isEqualTo("collection title")
        assertThat(publicCollectionEvent.description).isEqualTo("collection description")
        assertThat(publicCollectionEvent.isPublic).isTrue()
        assertThat(privateCollectionEvent.isPublic).isFalse()
        assertThat(publicCollectionEvent.videosIds).containsExactly(com.boclips.eventbus.domain.video.VideoId(videoId))
        assertThat(publicCollectionEvent.subjects).containsExactly(Subject(SubjectId("subject-id"), "subject name"))
        assertThat(publicCollectionEvent.ownerId.value).isEqualTo("user-id")
        assertThat(publicCollectionEvent.ageRange).isEqualTo(com.boclips.eventbus.domain.AgeRange(0, 23))
        assertThat(publicCollectionEvent.bookmarks).containsExactly(com.boclips.eventbus.domain.user.UserId("bookmarked-user-id"))
        assertThat(publicCollectionEvent.createdAt).isEqualTo("2017-11-10T01:02:03Z")
        assertThat(publicCollectionEvent.updatedAt).isEqualTo("2018-11-10T01:02:03Z")
    }

    @Test
    fun `converts video assets`() {
        val asset = VideoFactory.createVideoAsset(
            reference = "asset-id",
            sizeKb = 10,
            dimensions = Dimensions(1280, 720),
            bitrateKbps = 100
        )

        val eventAsset = converter.toAssetPayload(asset)

        assertThat(eventAsset.id).isEqualTo("asset-id")
        assertThat(eventAsset.sizeKb).isEqualTo(10)
        assertThat(eventAsset.dimensions.width).isEqualTo(1280)
        assertThat(eventAsset.dimensions.height).isEqualTo(720)
        assertThat(eventAsset.bitrateKbps).isEqualTo(100)
    }
}
