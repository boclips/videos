package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.category.CategoryWithAncestors
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoCategorySource
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.CategoryWithAncestorsFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createTopic
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import com.boclips.eventbus.domain.video.VideoType as EventBusVideoType

class EventConverterTest {
    private val converter = EventConverter()

    @Test
    fun `creates a video event object`() {
        val id = TestFactories.aValidId()
        val video = createVideo(
            videoId = id,
            title = "the title",
            description = "a description",
            channelId = ChannelId("id-666"),
            playback = TestFactories.createKalturaPlayback(
                duration = Duration.ofMinutes(2),
                originalDimensions = Dimensions(
                    1920,
                    1080
                ),
                assets = setOf(VideoFactory.createVideoAsset())
            ),
            subjects = setOf(TestFactories.createSubject(name = "physics")),
            types = listOf(VideoType.INSTRUCTIONAL_CLIPS),
            releasedOn = LocalDate.of(1939, 9, 1),
            ingestedAt = ZonedDateTime.of(2020, 11, 12, 13, 14, 15, 160000000, ZoneOffset.UTC),
            ageRange = AgeRange.of(min = 5, max = 10, curatedManually = true),
            promoted = true,
            keywords = listOf("key", "word"),
            videoReference = "video-reference",
            deactivated = true,
            categories = mapOf(
                CategorySource.CHANNEL to setOf(
                    CategoryWithAncestorsFactory.sample(
                        codeValue = "ZZ", description = "Lizards",
                        ancestors = setOf(
                            CategoryCode("Z")
                        )
                    )
                )
            )
        )

        val videoEvent = converter.toVideoPayload(video)

        val zzCategory = CategoryWithAncestors.builder()
            .code("ZZ")
            .description("Lizards")
            .ancestors(setOf("Z"))
            .build()

        assertThat(videoEvent.id.value).isEqualTo(id)
        assertThat(videoEvent.title).isEqualTo("the title")
        assertThat(videoEvent.description).isEqualTo("a description")
        assertThat(videoEvent.channelId.value).isEqualTo("id-666")
        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(videoEvent.subjects).hasSize(1)
        assertThat(videoEvent.subjects.first().name).isEqualTo("physics")
        assertThat(videoEvent.ageRange.min).isEqualTo(5)
        assertThat(videoEvent.ageRange.max).isEqualTo(10)
        assertThat(videoEvent.durationSeconds).isEqualTo(120)
        assertThat(videoEvent.type).isEqualTo(EventBusVideoType.INSTRUCTIONAL)
        assertThat(videoEvent.ingestedAt).isEqualTo("2020-11-12T13:14:15.16Z")
        assertThat(videoEvent.releasedOn).isEqualTo("1939-09-01")
        assertThat(videoEvent.assets).hasSize(1)
        assertThat(videoEvent.originalDimensions.width).isEqualTo(1920)
        assertThat(videoEvent.originalDimensions.height).isEqualTo(1080)
        assertThat(videoEvent.promoted).isTrue()
        assertThat(videoEvent.keywords).containsExactly("key", "word")
        assertThat(videoEvent.sourceVideoReference).isEqualTo("video-reference")
        assertThat(videoEvent.deactivated).isEqualTo(true)
        assertThat(videoEvent.categories[VideoCategorySource.CHANNEL]).containsExactlyInAnyOrder(zzCategory)
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
        val newsVideoEvent = converter.toVideoPayload(createVideo(types = listOf(VideoType.NEWS)))
        val stockVideoEvent = converter.toVideoPayload(createVideo(types = listOf(VideoType.STOCK)))
        val instructionalVideoEvent =
            converter.toVideoPayload(createVideo(types = listOf(VideoType.INSTRUCTIONAL_CLIPS)))

        assertThat(newsVideoEvent.type).isEqualTo(EventBusVideoType.NEWS)
        assertThat(stockVideoEvent.type).isEqualTo(EventBusVideoType.STOCK)
        assertThat(instructionalVideoEvent.type).isEqualTo(EventBusVideoType.INSTRUCTIONAL)
    }

    @Test
    fun `set video topics`() {
        val videoEvent = converter.toVideoPayload(
            createVideo(
                topics = setOf(
                    createTopic(
                        name = "Taxonomies",
                        language = Locale.forLanguageTag("fr_FR"),
                        confidence = 0.8,
                        parent = null
                    )
                )
            )
        )
        assertThat(videoEvent.topics.first().name).contains("Taxonomies")
        assertThat(videoEvent.topics.first().language).isEqualTo(Locale.forLanguageTag("fr_FR"))
        assertThat(videoEvent.topics.first().confidence).isEqualTo(0.8)
        assertThat(videoEvent.topics.first().parent).isNull()
    }

    @Test
    fun `set video topics for parent`() {
        val videoEvent = converter.toVideoPayload(
            createVideo(
                topics = setOf(
                    createTopic(
                        parent = createTopic(name = "Types of categorisation")
                    )
                )
            )
        )

        assertThat(videoEvent.topics.first().parent.name).isEqualTo("Types of categorisation")
    }

    @Test
    fun `deals with empty topic set`() {
        val videoEvent = converter.toVideoPayload(
            createVideo(
                topics = emptySet()
            )
        )

        assertThat(videoEvent.topics.size).isEqualTo(0)
    }

    @Test
    fun `deals with empty category set`() {
        val videoEvent = converter.toVideoPayload(
            createVideo(
                categories = emptyMap()
            )
        )

        assertThat(videoEvent.categories.size).isEqualTo(0)
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
    fun `sets playback ID when YouTube`() {
        val video = createVideo(
            playback = TestFactories.createYoutubePlayback(
                playbackId = PlaybackId(
                    type = YOUTUBE,
                    value = "playback id"
                )
            )
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.playbackId).isEqualTo("playback id")
    }

    @Test
    fun `sets playback ID when Kaltura`() {
        val video = createVideo(
            playback = TestFactories.createYoutubePlayback(
                playbackId = PlaybackId(
                    type = KALTURA,
                    value = "playback id"
                )
            )
        )

        val videoEvent = converter.toVideoPayload(video)

        assertThat(videoEvent.playbackId).isEqualTo("playback id")
    }

    @Test
    fun `creates a collection event object`() {
        val id = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()
        val publicCollection = TestFactories.createCollection(
            id = CollectionId(id),
            owner = "user-id",
            title = "collection title",
            videos = listOf(VideoId(videoId)),
            createdAt = ZonedDateTime.of(2017, 11, 10, 1, 2, 3, 0, ZoneOffset.UTC),
            updatedAt = ZonedDateTime.of(2018, 11, 10, 1, 2, 3, 0, ZoneOffset.UTC),
            discoverable = true,
            bookmarks = setOf(UserId("bookmarked-user-id")),
            subjects = setOf(TestFactories.createSubject(id = "subject-id", name = "subject name")),
            ageRangeMin = 0,
            ageRangeMax = 23,
            promoted = true
        )
        val nonDiscoverableCollection = TestFactories.createCollection(discoverable = false)

        val publicCollectionEvent = converter.toCollectionPayload(publicCollection)
        val nonDiscoverableCollectionEvent = converter.toCollectionPayload(nonDiscoverableCollection)

        assertThat(nonDiscoverableCollectionEvent.isDiscoverable).isFalse()

        assertThat(publicCollectionEvent.id.value).isEqualTo(id)
        assertThat(publicCollectionEvent.title).isEqualTo("collection title")
        assertThat(publicCollectionEvent.description).isEqualTo("collection description")
        assertThat(publicCollectionEvent.isDiscoverable).isTrue()
        assertThat(publicCollectionEvent.videosIds).containsExactly(com.boclips.eventbus.domain.video.VideoId(videoId))
        assertThat(publicCollectionEvent.subjects).containsExactly(Subject(SubjectId("subject-id"), "subject name"))
        assertThat(publicCollectionEvent.ownerId.value).isEqualTo("user-id")
        assertThat(publicCollectionEvent.ageRange).isEqualTo(com.boclips.eventbus.domain.AgeRange(0, 23))
        assertThat(publicCollectionEvent.bookmarks).containsExactly(com.boclips.eventbus.domain.user.UserId("bookmarked-user-id"))
        assertThat(publicCollectionEvent.createdAt).isEqualTo("2017-11-10T01:02:03Z")
        assertThat(publicCollectionEvent.updatedAt).isEqualTo("2018-11-10T01:02:03Z")
        assertThat(publicCollectionEvent.promoted).isTrue()
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
