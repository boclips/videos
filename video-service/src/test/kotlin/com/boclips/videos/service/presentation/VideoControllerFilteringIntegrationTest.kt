package com.boclips.videos.service.presentation

import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.users.api.response.organisation.OrganisationResource
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.*
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Duration
import java.time.LocalDate

class VideoControllerFilteringIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var tagVideo: TagVideo

    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String
    lateinit var customlyPricedOrganisationId: String

    @BeforeEach
    fun setUp() {
        customlyPricedOrganisationId = organisationWithCustomPrices(
            customPrices = DealResource.PricesResource(
                videoTypePrices = mapOf(
                    "STOCK" to DealResource.PriceResource(
                        amount = "10000",
                        currency = "USD"
                    ),
                    "NEWS" to DealResource.PriceResource(
                        amount = "15000",
                        currency = "USD"
                    )
                ),
                channelPrices = mapOf(
                    "channel-TED" to DealResource.PriceResource(
                        "400",
                        "USD"
                    )
                )
            )
        ).id

        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofMinutes(1),
            newChannelName = "enabled-cp",
            legalRestrictions = "None",
            ageRangeMin = 5, ageRangeMax = 7,
            types = listOf(VideoType.STOCK)
        ).value

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofMinutes(8),
            newChannelName = "enabled-cp2",
            ageRangeMin = 7, ageRangeMax = 10,
            types = listOf(VideoType.NEWS)
        ).value
    }

    @Test
    fun `can filter by query`() {
        mockMvc.perform(get("/v1/videos?query=jobs").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("elephants took out jobs")))
            .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("it's a video from youtube")))
            .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2017-02-11")))
            .andExpect(jsonPath("$._embedded.videos[0].createdBy", equalTo("enabled-cp2")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.id").exists())
            .andExpect(jsonPath("$._embedded.videos[0].playback.type", equalTo("YOUTUBE")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.duration", equalTo("PT8M")))
            .andExpect(jsonPath("$._embedded.videos[0].playback.downloadUrl").doesNotExist())
            .andExpect(
                jsonPath(
                    "$._embedded.videos[0].playback._links.thumbnail.href",
                    equalTo("https://youtube.com/thumb/yt-id-124.png")
                )
            )
            .andExpect(jsonPath("$._embedded.videos[0]._links.self.href", containsString("/videos/$youtubeVideoId")))
            .andExpect(jsonPath("$._embedded.videos[0].badges", equalTo(listOf("youtube"))))
    }

    @Test
    fun `can filter by channel id`() {

        val channelId = saveChannel(name = "test").id
        val videoId = saveVideo(existingChannelId = channelId.value)

        mockMvc.perform(get("/v1/videos?channel=${channelId.value}").asApiUser(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoId.value)))
    }

    @Test
    fun `can filter by channel ids`() {
        val contentProviderName = "enabled-cp2"
        val newVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            newChannelName = contentProviderName,
            legalRestrictions = "None"
        ).value
        val channel = getChannel(contentProviderName)

        mockMvc.perform(get("/v1/videos?channel=${channel.id.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(newVideoId)))
    }

    @Test
    fun `can filter by content type`() {
        val stockVideoId = saveVideo(title = "a stock video content", types = listOf(VideoType.STOCK))
        saveVideo(title = "this is a news video content", types = listOf(VideoType.NEWS))

        mockMvc.perform(get("/v1/videos?query=content&type=STOCK").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(stockVideoId.value)))
    }

    @Test
    fun `can find videos by single best for tag`() {
        val explainerTagUrl = createTag("explainer")
        val otherTagUrl = createTag("other")
        val testUser = UserFactory.sample()

        val firstExplainerVideoId = saveVideo(title = "Video with tags 1")
        tagVideo(TagVideoRequest(videoId = firstExplainerVideoId.value, tagUrl = explainerTagUrl), testUser)

        val secondExplainerVideoId = saveVideo(title = "Video with tags 2")
        tagVideo(TagVideoRequest(videoId = secondExplainerVideoId.value, tagUrl = explainerTagUrl), testUser)

        val otherVideo = saveVideo(title = "Video with tags 3")
        tagVideo(TagVideoRequest(videoId = otherVideo.value, tagUrl = otherTagUrl), testUser)

        mockMvc.perform(get("/v1/videos?query=tags&best_for=explainer").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[*].id",
                    containsInAnyOrder(firstExplainerVideoId.value, secondExplainerVideoId.value)
                )
            )
    }

    @Test
    fun `can find videos by promoted flag`() {
        val promotedVideoId = saveVideo(title = "ben poos elephants")
        val unpromotedVideoId = saveVideo(title = "Video about elephants")

        setPromoted(promotedVideoId.value, true)
        setPromoted(unpromotedVideoId.value, false)

        mockMvc.perform(get("/v1/videos?promoted=true").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(promotedVideoId.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(unpromotedVideoId.value))))
    }

    @Test
    fun `can find videos with subjects that have been manually tagged`() {
        saveVideo(title = "my subject will NOT be edited")

        val editedVideo = saveVideo(title = "my subject will be edited")
        val newSubject = saveSubject("Maths")

        mockMvc.perform(
            patch("/v1/videos/${editedVideo.value}")
                .content("""{ "subjectIds": ["${newSubject.id.value}"] }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )

        mockMvc.perform(get("/v1/videos?query=subject&subjects_set_manually=true").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(editedVideo.value)))
    }

    @Test
    fun `can filter by specified duration lower and upper bound`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration_min=PT0M&duration_max=PT2M").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by multiple durations`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration=PT0M-PT2M,PT2M-PT5M").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by single duration`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration=PT7M").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by single age range`() {
        mockMvc.perform(get("/v1/videos?age_range=5-7").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by multiple age ranges`() {
        mockMvc.perform(get("/v1/videos?age_range=5-7&age_range=7-10").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by age range lower and upper bound`() {
        mockMvc.perform(get("/v1/videos?query=elephants&age_range_min=5&age_range_max=7").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=boclips").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by video id`() {
        mockMvc.perform(get("/v1/videos?id=$kalturaVideoId").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by video ids`() {
        mockMvc.perform(get("/v1/videos?id=$kalturaVideoId&id=$youtubeVideoId").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by channel`() {
        val channel = getChannel("enabled-cp2")

        mockMvc.perform(get("/v1/videos?channel=${channel.id.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by channels`() {
        val newVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            newChannelName = "cp3",
            legalRestrictions = "None"
        ).value

        val channel1 = getChannel("enabled-cp2")
        val channel2 = getChannel("cp3")

        mockMvc.perform(get("/v1/videos?channel=${channel1.id.value}&channel=${channel2.id.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(newVideoId)))
    }

    @Test
    fun `can filter by specified released data`() {
        mockMvc.perform(
            get("/v1/videos?query=elephants&released_date_from=2018-01-11&released_date_to=2018-03-11").asTeacher(
                email = userAssignedToOrganisation().idOrThrow().value
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by query and subject`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            newChannelName = "enabled-cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths").id
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?query=elephants&subject=${subjectId.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoId)))
    }

    @Test
    fun `can filter by subject`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            newChannelName = "enabled-cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths").id
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?subject=${subjectId.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoId)))
    }

    @Test
    fun `can filter by subjects`() {
        saveVideo()
        val mathsVideoId = saveVideo().value
        val englishVideoId = saveVideo().value

        val mathsId = saveSubject("Maths").id
        val englishId = saveSubject("English").id
        setVideoSubjects(mathsVideoId, mathsId)
        setVideoSubjects(englishVideoId, englishId)

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value}&subject=${englishId.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
    }

    @Test
    fun `can filter by subjects with comma syntax`() {
        saveVideo()
        val mathsVideoId = saveVideo().value
        val englishVideoId = saveVideo().value

        val mathsId = saveSubject("Maths").id
        val englishId = saveSubject("English").id
        setVideoSubjects(mathsVideoId, mathsId)
        setVideoSubjects(englishVideoId, englishId)

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value},${englishId.value}").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
    }

    @Test
    fun `can filter by video resource types`() {
        saveVideo()
        val videoWithActivity = saveVideo()
        val videoWithLessonPlan = saveVideo()

        addVideoAttachment(
            attachment = AttachmentRequest(
                linkToResource = "https://www.boclips.com",
                type = "ACTIVITY",
                description = "a description"
            ),
            videoId = videoWithActivity
        )
        addVideoAttachment(
            attachment = AttachmentRequest(
                linkToResource = "https://www.boclips.com",
                type = "LESSON_PLAN",
                description = "a description"
            ),
            videoId = videoWithLessonPlan
        )

        mockMvc.perform(get("/v1/videos?resource_types=LESSON_PLAN").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoWithLessonPlan.value)))
    }

    @Test
    fun `can filter by video price`() {
        val user = userAssignedToOrganisation(
            organisationId = customlyPricedOrganisationId
        )

        mockMvc.perform(get("/v1/videos?prices=10000").asBoclipsWebAppUser(email = user.idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by category if video's channel was tagged`() {
        val includedCategory =
            addCategory(CategoryFactory.sample(code = "AB", description = "French", parentCode = "A"))
        val excludedCategory =
            addCategory(CategoryFactory.sample(code = "XY", description = "Spanish", parentCode = "X"))
        val abChannel = saveChannel(name = "AB channel")
        val xyChannel = saveChannel(name = "XY channel")
        val video = saveVideo(existingChannelId = abChannel.id.value)

        saveVideo(existingChannelId = xyChannel.id.value)
        tagChannelWithCategory(category = includedCategory, channelId = ChannelId(abChannel.id.value))
        tagChannelWithCategory(category = excludedCategory, channelId = ChannelId(xyChannel.id.value))

        mockMvc.perform(get("/v1/videos?category_code=AB").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(video.value)))
    }

    @Test
    fun `filtering by category returns videos tagged with a ancestor categories `() {
        val includedCategory =
            addCategory(CategoryFactory.sample(code = "AB", description = "French", parentCode = "A"))
        val includedAncestorCategory =
            addCategory(CategoryFactory.sample(code = "A", description = "French", parentCode = null))
        val excludedCategory =
            addCategory(CategoryFactory.sample(code = "XY", description = "Spanish", parentCode = "X"))
        val abChannel = saveChannel(name = "AB channel")
        val aChannel = saveChannel(name = "A channel")
        val xyChannel = saveChannel(name = "XY channel")
        val video = saveVideo(existingChannelId = abChannel.id.value)
        val videoWithAncestor = saveVideo(existingChannelId = aChannel.id.value)

        saveVideo(existingChannelId = xyChannel.id.value)
        tagChannelWithCategory(category = includedCategory, channelId = ChannelId(abChannel.id.value))
        tagChannelWithCategory(category = includedAncestorCategory, channelId = ChannelId(aChannel.id.value))
        tagChannelWithCategory(category = excludedCategory, channelId = ChannelId(xyChannel.id.value))

        mockMvc.perform(get("/v1/videos?category_code=A").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(video.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(videoWithAncestor.value)))
    }

    @Test
    fun `can filter by mulitple categories`() {
        val includedCategory = addCategory(
            CategoryFactory.sample(code = "AB", description = "French", parentCode = "A")
        )
        val includedOtherCategory = addCategory(
            CategoryFactory.sample(code = "AC", description = "French", parentCode = "A")
        )
        val excludedCategory = addCategory(
            CategoryFactory.sample(code = "XY", description = "Spanish", parentCode = "X")
        )
        val abChannel = saveChannel(name = "AB channel")
        val aChannel = saveChannel(name = "A channel")
        val xyChannel = saveChannel(name = "XY channel")

        val video = saveVideo(existingChannelId = abChannel.id.value)
        val videoWithAncestor = saveVideo(existingChannelId = aChannel.id.value)
        saveVideo(existingChannelId = xyChannel.id.value)

        tagChannelWithCategory(category = includedCategory, channelId = ChannelId(abChannel.id.value))
        tagChannelWithCategory(category = includedOtherCategory, channelId = ChannelId(aChannel.id.value))
        tagChannelWithCategory(category = excludedCategory, channelId = ChannelId(xyChannel.id.value))

        mockMvc.perform(get("/v1/videos?category_code=A,AC").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(video.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(videoWithAncestor.value)))
    }

    @Test
    fun `can filter by video tagged categories`() {
        val greatCategory = addCategory(
            CategoryFactory.sample(code = "ACAB", description = "Great Category", parentCode = "ACA")
        )
        val goodCategory = addCategory(
            CategoryFactory.sample(code = "CBA", description = "Good Category", parentCode = "CB")
        )
        val rubbishCategory = addCategory(
            CategoryFactory.sample(code = "XYZ", description = "Rubbish Category", parentCode = "XY")
        )

        val greatVideo = saveVideo(categories = listOf(greatCategory.code.value), newChannelName = "HI")
        val goodVideo = saveVideo(categories = listOf(goodCategory.code.value), newChannelName = "Hello")
        saveVideo(categories = listOf(rubbishCategory.code.value), newChannelName = "BYE")

        mockMvc.perform(get("/v1/videos?category_code=ACAB,CBA").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(greatVideo.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(goodVideo.value)))
    }

    @Test
    fun `sort by rating`() {
        val firstTitle = "low-rated"
        val secondTitle = "high-rated"
        val thirdTitle = "mid-rated"

        val firstVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-1", type = PlaybackProviderType.KALTURA),
            title = firstTitle
        )

        val secondVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-2", type = PlaybackProviderType.YOUTUBE),
            title = secondTitle
        )

        val thirdVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-3", type = PlaybackProviderType.KALTURA),
            title = thirdTitle
        )

        setRating(firstVideoId, 0)
        setRating(secondVideoId, 5)
        setRating(thirdVideoId, 3)

        // first page
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=0").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(secondTitle)))
            .andExpect(jsonPath("$._embedded.videos[1].title", equalTo(thirdTitle)))

        // second page
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=1").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(firstTitle)))
    }

    @Test
    fun `sort by ingest date`() {
        saveVideo(title = "oldest ingested video")
        saveVideo(title = "newer ingested video")
        saveVideo(title = "newest ingested video")

        mockMvc.perform(get("/v1/videos?query=ingested&sort_by=INGEST_ASC&size=3").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("oldest ingested video")))
            .andExpect(jsonPath("$._embedded.videos[1].title", equalTo("newer ingested video")))
            .andExpect(jsonPath("$._embedded.videos[2].title", equalTo("newest ingested video")))
    }

    @Test
    fun `sort by category codes`() {
        saveVideo(title = "A category", categories = listOf("A"))
        saveVideo(title = "B category", categories = listOf("B"))
        saveVideo(title = "Empty category codes", categories = emptyList())

        mockMvc.perform(get("/v1/videos?query=category&sort_by=UNTAGGED_CATEGORIES").asBoclipsEmployee(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("Empty category codes")))
    }

    @Test
    fun `returns 400 with invalid source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=invalidoops").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 400 with invalid duration`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration_min=invalidoops").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 400 with invalid date filter`() {
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_from=invalidoops").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_to=invalidoops").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `it sorts news by releaseDate descending`() {
        val today = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            types = listOf(VideoType.NEWS)
        ).value
        val yesterday = saveVideo(
            title = "Yesterday Video",
            date = LocalDate.now().minusDays(1).toString(),
            types = listOf(VideoType.NEWS)
        ).value
        val tomorrow = saveVideo(
            title = "Tomorrow Video",
            date = LocalDate.now().plusDays(1).toString(),
            types = listOf(VideoType.NEWS)
        ).value

        val resultActions = mockMvc.perform(
            get("/v1/videos?query=video&sort_by=RELEASE_DATE")
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee(email = userAssignedToOrganisation().idOrThrow().value)
        )

        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(tomorrow)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(today)))
            .andExpect(jsonPath("$._embedded.videos[2].id", equalTo(yesterday)))
    }

    @Test
    fun `returns empty videos array when nothing matches`() {
        mockMvc.perform(get("/v1/videos?query=whatdohorseseat").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
    }

    @Test
    fun `sends 'videoSearched' event when searching for videos`() {

        val channelId = saveChannel(name = "test").id
        saveVideo(existingChannelId = channelId.value)

        mockMvc.perform(
            get(
                "/v1/videos?channel=${channelId.value}&query=dragons&duration=PT2M-PT5M" +
                    "&duration_facets=PT0S-PT2M,PT2M-PT5M,PT5M-PT10M,PT10M-PT20M,PT20M-PT24H" +
                    "&age_range=9-11,11-14&age_range_facets=3-5,5-9,9-11,11-14,14-16,16-99&size=10&page=0" +
                    "&subject=5cb499c9fd5beb428189454c&type=INSTRUCTIONAL&resource_type_facets=Activity,Lesson+Guide"
            )
                .asApiUser(email = userAssignedToOrganisation().idOrThrow().value)
        )
            .andExpect(status().isOk)

        val event = fakeEventBus.getEventOfType(VideosSearched::class.java)
        Assertions.assertThat(event.query).isEqualTo("dragons")
        Assertions.assertThat(event.queryParams).hasSize(11)
        Assertions.assertThat(event.queryParams["channel"]).containsExactly(channelId.value)
        Assertions.assertThat(event.queryParams["duration"]).containsExactly("PT2M-PT5M")
        Assertions.assertThat(event.queryParams["duration_facets"]).hasSize(5)
    }

    @Test
    fun `returns bad request when the pagination is too deep`() {
        mockMvc.perform(get("/v1/videos?query=jobs&page=1000&size=10").asApiUser())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()

        mockMvc.perform(get("/v1/videos?query=jobs&page=99&size=101").asApiUser())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    private fun getRatingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        return JsonPath.parse(videoResponse).read("$._links.rate.href")
    }

    private fun createTag(name: String): String {
        return mockMvc.perform(
            post("/v1/tags").content(
                """
                {
                  "label": "$name",
                  "UserId": "User-1"
                }
                """.trimIndent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        ).andReturn().response.getHeader("Location")!!
    }

    private fun setPromoted(videoId: String, promoted: Boolean): ResultActions {
        return mockMvc.perform(
            patch("/v1/videos/$videoId")
                .content("""{ "promoted": $promoted }""".trimIndent())
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }

    private fun setRating(videoId: VideoId, rating: Int) {
        val rateUrl = getRatingLink(videoId.value)

        mockMvc.perform(patch(rateUrl, rating).asTeacher()).andExpect(status().isOk)
    }

    private fun organisationWithCustomPrices(customPrices: DealResource.PricesResource? = null): OrganisationResource {
        return organisationsClient.add(
            OrganisationResourceFactory.sample(
                deal = DealResource(
                    prices = customPrices,
                    accessExpiresOn = null,
                    billing = false,
                    contentPackageId = null
                )
            )
        )
    }

    private fun userAssignedToOrganisation(organisationId: String, id: String = "the@teacher.com"): User {
        val userResource = usersClient.add(
            UserResourceFactory.sample(
                id = id,
                organisation = OrganisationResourceFactory.sampleDetails().copy(id = organisationId)
            )
        )
        return UserFactory.sample(
            id = userResource.id
        )
    }
}
