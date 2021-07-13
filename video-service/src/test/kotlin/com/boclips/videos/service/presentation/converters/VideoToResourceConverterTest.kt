package com.boclips.videos.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.prices.PricedVideo
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Duration
import java.util.Currency
import java.util.Locale

class VideoToResourceConverterTest {
    private lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    private lateinit var videosLinkBuilder: VideosLinkBuilder
    private lateinit var videoToResourceConverter: VideoToResourceConverter
    private lateinit var videoChannelService: VideoChannelService
    private lateinit var getSubjects: GetSubjects

    private val kalturaVideo = createVideo(
        title = "Do what you love",
        description = "Best bottle slogan",
        additionalDescription = "additional description",
        channelName = "WeWork",
        channelVideoId = "111",
        types = listOf(VideoType.NEWS),
        subjects = setOf(TestFactories.createSubject(id = "maths-subject-id", name = "Maths")),
        legalRestrictions = "None",
        voice = Voice.WithVoice(language = Locale("khm"), transcript = Transcript(content = "Hello there")),
        ageRange = AgeRange.of(min = 5, max = 11, curatedManually = false),
        ratings = listOf(
            UserRating(
                rating = 3,
                userId = UserId(
                    "user-id"
                )
            )
        ),
        tags = listOf(TestFactories.createUserTag("tag-id", "tag-label", "user-id")),
        promoted = true,
        attachments = listOf(
            AttachmentFactory.sample(
                description = "some description",
                type = AttachmentType.ACTIVITY,
                linkToResource = "link"
            )
        ),
        contentWarnings = listOf(
            ContentWarning(id = ContentWarningId(ObjectId().toHexString()), label = "Warning"),
            ContentWarning(id = ContentWarningId(ObjectId().toHexString()), label = "Other disclaimer")
        ),
        categories = mapOf(
            CategorySource.CHANNEL to setOf(
                CategoryWithAncestors(
                    codeValue = CategoryCode("A"), description = "Test", ancestors = setOf(CategoryCode("A")),

                )
            ),
            CategorySource.MANUAL to setOf(
                CategoryWithAncestors(
                    codeValue = CategoryCode("B"), description = "Test", ancestors = setOf(CategoryCode("B"))

                )
            )
        ),
    )

    private val youtubeVideo = createVideo(
        title = "Do what you love on youtube",
        description = "Best bottle slogan",
        additionalDescription = "additional description",
        channelName = "JacekWork",
        channelVideoId = "222",
        playback = TestFactories.createYoutubePlayback(),
        types = listOf(VideoType.INSTRUCTIONAL_CLIPS),
        subjects = setOf(TestFactories.createSubject(id = "biology-subject-id", name = "Biology")),
        legalRestrictions = "Many",
        tags = listOf(TestFactories.createUserTag("tag-id", "tag-label", "user-id")),
        attachments = listOf(
            AttachmentFactory.sample(
                description = "some description",
                type = AttachmentType.ACTIVITY,
                linkToResource = "link"
            )
        )
    )

    @BeforeEach
    fun setUp() {
        videosLinkBuilder = mock()
        videoChannelService = mock()
        getSubjects = mock()

        playbackToResourceConverter =
            PlaybackToResourceConverter(
                mock(),
                PlaybacksLinkBuilder(TestKalturaClient())
            )
        videoToResourceConverter =
            VideoToResourceConverter(
                videosLinkBuilder,
                playbackToResourceConverter,
                AttachmentToResourceConverter(mock()),
                ContentWarningToResourceConverter(mock()),
                videoChannelService,
                getSubjects
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
        assertThat(videoResource.additionalDescription).isNull()
        assertThat(videoResource.createdBy).isEqualTo(videoResource.channel)
        assertThat(videoResource.channel).isNull()
        assertThat(videoResource.channelVideoId).isNull()
        assertThat(videoResource.subjects).isNullOrEmpty()
        assertThat(videoResource.playback).isNull()
        assertThat(videoResource.badges).isNullOrEmpty()
    }

    @Test
    fun `converts a video from Kaltura`() {
        val price = Price(
            amount = BigDecimal.valueOf(300),
            currency = Currency.getInstance("USD")
        )
        val pricedVideo = PricedVideo(
            video = kalturaVideo,
            price = price
        )
        val videoResource = videoToResourceConverter.convert(
            video = pricedVideo,
            user = UserFactory.sample(id = "user-id")
        )

        assertThat(videoResource.title).isEqualTo("Do what you love")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.additionalDescription).isEqualTo("additional description")
        assertThat(videoResource.createdBy).isEqualTo(videoResource.channel)
        assertThat(videoResource.channel).isEqualTo("WeWork").isEqualTo(videoResource.channel)
        assertThat(videoResource.channelVideoId).isEqualTo("111").isEqualTo(videoResource.channelVideoId)
        assertThat(videoResource.subjects).containsExactly(
            SubjectResource(
                id = "maths-subject-id",
                name = "Maths"
            )
        )
        assertThat(videoResource.types!!.first().id).isEqualTo(1)
        assertThat(videoResource.types!!.first().name).isEqualTo("News")
        assertThat(videoResource.badges).isEqualTo(setOf("ad-free"))
        assertThat(videoResource.legalRestrictions).isEqualTo("None")

        assertThat(videoResource.ageRange!!.min).isEqualTo(5)
        assertThat(videoResource.ageRange!!.max).isEqualTo(11)
        assertThat(videoResource.rating).isEqualTo(3.0)
        assertThat(videoResource.yourRating).isEqualTo(3.0)
        assertThat(videoResource.bestFor!!.map { it.label }).containsOnly("tag-label")
        assertThat(videoResource.promoted).isEqualTo(true)
        assertThat(videoResource.language?.code).isEqualTo("khm")
        assertThat(videoResource.language?.displayName).isEqualTo("Central Khmer")
        assertThat(videoResource.hasTranscripts).isEqualTo(true)
        assertThat(videoResource.isVoiced).isEqualTo(true)
        assertThat(videoResource.price?.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(videoResource.price?.amount?.intValueExact()).isEqualTo(300)

        assertThat(videoResource.attachments).hasSize(1)
        assertThat(videoResource.attachments[0].id).isNotNull()
        assertThat(videoResource.attachments[0].type).isEqualTo("ACTIVITY")
        assertThat(videoResource.attachments[0].description).isEqualTo("some description")

        assertThat(videoResource.contentWarnings).hasSize(2)
        assertThat(videoResource.contentWarnings!![0].label).isEqualTo("Warning")
        assertThat(videoResource.contentWarnings!![1].label).isEqualTo("Other disclaimer")

        val playbackResource = videoResource.playback!! as StreamPlaybackResource
        assertThat(playbackResource.type).isEqualTo("STREAM")
        assertThat(playbackResource.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat(playbackResource.id).isEqualTo("entry-id")
        assertThat(playbackResource.referenceId).isEqualTo("555")
        assertThat(videoResource.taxonomy?.channel?.categories!![0].codeValue).isEqualTo("A")
        assertThat(videoResource.taxonomy?.channel?.categories!![0].ancestors).contains("A")
        assertThat(videoResource.taxonomy?.manual?.categories!![0].codeValue).isEqualTo("B")
        assertThat(videoResource.taxonomy?.manual?.categories!![0].ancestors).contains("B")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = videoToResourceConverter.convert(youtubeVideo, UserFactory.sample())

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.additionalDescription).isEqualTo("additional description")
        assertThat(videoResource.createdBy).isEqualTo(videoResource.channel)
        assertThat(videoResource.channel).isEqualTo("JacekWork")
        assertThat(videoResource.channelVideoId).isEqualTo("222")
        assertThat(videoResource.subjects).containsExactly(
            SubjectResource(
                id = "biology-subject-id",
                name = "Biology"
            )
        )
        assertThat(videoResource.types!!.first().id).isEqualTo(3)
        assertThat(videoResource.types!!.first().name).isEqualTo("Instructional Clips")
        assertThat(videoResource.price).isNull()

        assertThat((videoResource.playback!! as YoutubePlaybackResource).type).isEqualTo("YOUTUBE")
        assertThat((videoResource.playback!! as YoutubePlaybackResource).duration).isEqualTo(Duration.ofSeconds(21))
        assertThat((videoResource.playback!! as YoutubePlaybackResource).id).isEqualTo("444")

        assertThat(videoResource.badges).isEqualTo(setOf("youtube"))
        assertThat(videoResource.legalRestrictions).isEqualTo("Many")
        assertThat(videoResource.bestFor!!.map { it.label }).containsOnly("tag-label")

        assertThat(videoResource.attachments).hasSize(1)
        assertThat(videoResource.attachments[0].id).isNotNull()
        assertThat(videoResource.attachments[0].type).isEqualTo("ACTIVITY")
        assertThat(videoResource.attachments[0].description).isEqualTo("some description")
    }

    @Test
    fun `omits attachments + playback details except thumbnail when omitProtectedAttributes is true`() {
        val videoResource = videoToResourceConverter.convert(
            youtubeVideo,
            user = UserFactory.sample(),
            omitProtectedAttributes = true
        )

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        val playback = videoResource.playback!! as YoutubePlaybackResource

        assertThat(playback._links!!.keys).hasSize(1)
        assertThat(playback._links!!["thumbnail"]).isNotNull
        assertThat(videoResource.attachments).hasSize(0)
    }

    @Test
    fun `converts bestFor to empty list when video does not have a tag`() {
        val video = createVideo(
            title = "Do what you love on youtube",
            description = "Best bottle slogan",
            channelName = "JacekWork",
            channelVideoId = "222",
            playback = TestFactories.createYoutubePlayback(),
            types = listOf(VideoType.INSTRUCTIONAL_CLIPS),
            subjects = setOf(TestFactories.createSubject(id = "biology-subject-id", name = "Biology")),
            legalRestrictions = "Many",
            tags = emptyList()
        )

        val videoResource = videoToResourceConverter.convert(video, UserFactory.sample())

        assertThat(videoResource.bestFor).isEmpty()
    }

    @Test
    fun `converts heterogenous video lists`() {
        val resultResource = videoToResourceConverter
            .convert(videos = listOf(youtubeVideo, kalturaVideo), user = UserFactory.sample())

        assertThat(resultResource.map { it.playback!!.type }).containsExactlyInAnyOrder(
            "STREAM",
            "YOUTUBE"
        )
    }

    @Test
    fun `produces facets if they exist on the search results`() {
        `when`(videoChannelService.findAllByIds(any())).thenReturn(
            listOf(
                Channel(channelId = ChannelId("channel-id"), name = "TED")
            )
        )

        val resultResource = videoToResourceConverter.convert(
            resultsPage = ResultsPage(
                elements = listOf(),
                counts = VideoCounts(
                    total = 10,
                    subjects = listOf(SubjectFacet(subjectId = SubjectId("id"), total = 100)),
                    ageRanges = listOf(
                        AgeRangeFacet(
                            ageRangeId = AgeRangeId(
                                "3-5"
                            ),
                            total = 3
                        ),
                        AgeRangeFacet(
                            ageRangeId = AgeRangeId(
                                "5-11"
                            ),
                            total = 1
                        )
                    ),
                    durations = listOf(DurationFacet(durationId = "PT0S-PT1M", total = 10)),
                    attachmentTypes = listOf(AttachmentTypeFacet(attachmentType = "Activity", total = 5)),
                    channels = listOf(
                        ChannelFacet(channelId = ChannelId("channel-id"), total = 7),
                        ChannelFacet(channelId = ChannelId("non-existing-channel-id"), total = 9)
                    ),
                    videoTypes = listOf(VideoTypeFacet(typeId = "stock", total = 10)),
                    prices = listOf(PriceFacet(price = "20000", total = 10))
                ),
                pageInfo = PageInfo(
                    hasMoreElements = false,
                    totalElements = 10,
                    pageRequest = PageRequest(page = 0, size = 10)
                )
            ),
            user = UserFactory.sample()
        )

        assertThat(resultResource._embedded.facets?.subjects?.get("id")?.hits).isEqualTo(100)

        assertThat(resultResource._embedded.facets?.ageRanges?.get("3-5")?.hits).isEqualTo(3)
        assertThat(resultResource._embedded.facets?.ageRanges?.get("5-11")?.hits).isEqualTo(1)

        assertThat(resultResource._embedded.facets?.durations?.get("PT0S-PT1M")?.hits).isEqualTo(10)

        assertThat(resultResource._embedded.facets?.resourceTypes?.get("Activity")?.hits).isEqualTo(5)

        assertThat(resultResource._embedded.facets?.channels?.size).isEqualTo(1)
        assertThat(resultResource._embedded.facets?.channels?.get("channel-id")?.hits).isEqualTo(7)
        assertThat(resultResource._embedded.facets?.channels?.get("channel-id")?.id).isEqualTo("channel-id")
        assertThat(resultResource._embedded.facets?.channels?.get("channel-id")?.name).isEqualTo("TED")

        assertThat(resultResource._embedded.facets?.videoTypes?.size).isEqualTo(1)
        assertThat(resultResource._embedded.facets?.videoTypes?.get("STOCK")?.hits).isEqualTo(10)

        assertThat(resultResource._embedded.facets?.prices?.size).isEqualTo(1)
        assertThat(resultResource._embedded.facets?.prices?.get("20000")?.hits).isEqualTo(10)
    }

    @Test
    fun `omits facets if they don't exist on the search results`() {
        val resultResource = videoToResourceConverter.convert(
            resultsPage = ResultsPage(
                elements = listOf(),
                counts = null,
                pageInfo = PageInfo(
                    hasMoreElements = false,
                    totalElements = 10,
                    pageRequest = PageRequest(page = 0, size = 10)
                )
            ),
            user = UserFactory.sample()
        )

        assertThat(resultResource._embedded.facets).isNull()
    }
}
