package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.service.application.video.TagVideo
import com.boclips.videos.service.domain.model.BoundedAgeRange
import com.boclips.videos.service.domain.model.UnboundedAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.MongoVideoRepository.Companion.collectionName
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import com.boclips.videos.service.testsupport.asTeacher
import com.damnhandy.uri.template.UriTemplate
import com.jayway.jsonpath.JsonPath
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class VideoControllerFilteringIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var tagVideo: TagVideo

    lateinit var disabledVideoId: String
    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofMinutes(1),
            contentProvider = "enabled-cp",
            legalRestrictions = "None",
            ageRange = BoundedAgeRange(min = 5, max = 7)
        ).value

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofMinutes(8),
            contentProvider = "enabled-cp2",
            ageRange = BoundedAgeRange(min = 7, max = 10)
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2018-05-10",
            duration = Duration.ofMinutes(5),
            contentProvider = "disabled-cp",
            ageRange = UnboundedAgeRange,
            distributionMethods = emptySet()
        ).value
    }

    @Test
    fun `can filter by query and return youtube videos`() {
        mockMvc.perform(get("/v1/videos?query=jobs").asTeacher())
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
    fun `can filter by content type`() {
        val stockVideoId = saveVideo(title = "content type filtering", type = ContentType.STOCK)
        saveVideo(title = "content type filtering", type = ContentType.NEWS)

        mockMvc.perform(get("/v1/videos?query=content&type=STOCK").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(stockVideoId.value)))
    }

    @Test
    fun `can find by is_classroom`() {
        val notClassroomVideoId = saveVideo(title = "not suitable for the classroom", type = ContentType.STOCK)
        val classroomVideoId =
            saveVideo(title = "suitable for the classroom", type = ContentType.INSTRUCTIONAL_CLIPS)

        mockMvc.perform(get("/v1/videos?query=suitable for&is_classroom=true").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[*].id", hasItem(classroomVideoId.value)))
            .andExpect(jsonPath("$._embedded.videos[*].id", not(hasItem(notClassroomVideoId.value))))
    }

    @Test
    fun `can find videos by single best for tag`() {
        val explainerTagUrl = createTag("explainer")
        val otherTagUrl = createTag("other")
        val testUser = UserFactory.sample()

        val firstExplainerVideoId = saveVideo(title = "Video with tags")
        tagVideo(TagVideoRequest(videoId = firstExplainerVideoId.value, tagUrl = explainerTagUrl), testUser)

        val secondExplainerVideoId = saveVideo(title = "Video with tags")
        tagVideo(TagVideoRequest(videoId = secondExplainerVideoId.value, tagUrl = explainerTagUrl), testUser)

        val otherVideo = saveVideo(title = "Video with tags")
        tagVideo(TagVideoRequest(videoId = otherVideo.value, tagUrl = otherTagUrl), testUser)

        mockMvc.perform(get("/v1/videos?query=tags&best_for=explainer").asTeacher())
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

        mockMvc.perform(get("/v1/videos?promoted=true").asTeacher())
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
            patch("/v1/videos/${editedVideo.value}?subjectIds=${newSubject.id.value}")
                .asBoclipsEmployee()
        )

        mockMvc.perform(get("/v1/videos?query=subject&subjects_set_manually=true").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(editedVideo.value)))
    }

    @Test
    fun `can filter by id`() {
        mockMvc.perform(get("/v1/videos?query=id:$kalturaVideoId,-1").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))

            .andExpect(jsonPath("$.page.size", equalTo(100)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(1)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
    }

    @Test
    fun `can filter by specified duration lower and upper bound`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration_min=PT0M&duration_max=PT2M").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by multiple durations`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration=PT0M-PT2M,PT2M-PT5M").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by single duration`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration=PT7M").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by single age range`() {
        mockMvc.perform(get("/v1/videos?age_range=5-7").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by multiple age ranges`() {
        mockMvc.perform(get("/v1/videos?age_range=5-7&age_range=7-10").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by age range lower and upper bound`() {
        mockMvc.perform(get("/v1/videos?query=elephants&age_range_min=5&age_range_max=7").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=boclips").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(kalturaVideoId)))
    }

    @Test
    fun `can filter by content partner`() {
        mockMvc.perform(get("/v1/videos?content_partner=enabled-cp2").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
    }

    @Test
    fun `can filter by content partners`() {
        val newVideoId = saveVideo(
            playbackId = PlaybackId(value = "ref-id-876", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofSeconds(23),
            contentProvider = "cp3",
            legalRestrictions = "None"
        ).value

        mockMvc.perform(get("/v1/videos?content_partner=enabled-cp2&content_partner=cp3").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(youtubeVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(newVideoId)))
    }

    @Test
    fun `can filter by specified released data`() {
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_from=2018-01-11&released_date_to=2018-03-11").asTeacher())
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
            contentProvider = "enabled-cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths").id
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?query=elephants&subject=${subjectId.value}").asTeacher())
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
            contentProvider = "enabled-cp",
            legalRestrictions = "None"
        ).value

        val subjectId = saveSubject("Maths").id
        setVideoSubjects(videoId, subjectId)

        mockMvc.perform(get("/v1/videos?subject=${subjectId.value}").asTeacher())
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

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value}&subject=${englishId.value}").asTeacher())
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

        mockMvc.perform(get("/v1/videos?subject=${mathsId.value},${englishId.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(mathsVideoId)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(englishVideoId)))
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
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=0").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(secondTitle)))
            .andExpect(jsonPath("$._embedded.videos[1].title", equalTo(thirdTitle)))

        // second page
        mockMvc.perform(get("/v1/videos?sort_by=RATING&size=2&page=1").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.videos[0].title", equalTo(firstTitle)))
    }

    @Test
    fun `returns 400 with invalid source`() {
        mockMvc.perform(get("/v1/videos?query=elephants&source=invalidoops").asTeacher())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 400 with invalid duration`() {
        mockMvc.perform(get("/v1/videos?query=elephants&duration_min=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `returns 400 with invalid date filter`() {
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_from=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
        mockMvc.perform(get("/v1/videos?query=elephants&released_date_to=invalidoops").asTeacher())
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `it sorts news by releaseDate descending`() {
        val today = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            type = ContentType.NEWS
        ).value
        val yesterday = saveVideo(
            title = "Yesterday Video",
            date = LocalDate.now().minusDays(1).toString(),
            type = ContentType.NEWS
        ).value
        val tomorrow = saveVideo(
            title = "Tomorrow Video",
            date = LocalDate.now().plusDays(1).toString(),
            type = ContentType.NEWS
        ).value

        val resultActions = mockMvc.perform(
            get("/v1/videos?query=video&sort_by=RELEASE_DATE")
                .contentType(MediaType.APPLICATION_JSON).asBoclipsEmployee()
        )

        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(tomorrow)))
            .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(today)))
            .andExpect(jsonPath("$._embedded.videos[2].id", equalTo(yesterday)))
    }

    @Test
    fun `returns empty videos array when nothing matches`() {
        mockMvc.perform(get("/v1/videos?query=whatdohorseseat").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
    }

    private fun getRatingLink(videoId: String): String {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        return JsonPath.parse(videoResponse).read("$._links.rate.href")
    }

    private fun getUpdateLink(videoId: String): UriTemplate {
        val videoResponse = mockMvc.perform(get("/v1/videos/$videoId").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val link = JsonPath.parse(videoResponse).read<String>("$._links.update.href")

        return UriTemplate.fromTemplate(link)
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
        val updateLink = getUpdateLink(videoId).expand(mapOf("promoted" to promoted))

        return mockMvc.perform(patch(URI.create(updateLink)).asBoclipsEmployee())
    }

    private fun setRating(videoId: VideoId, rating: Int) {
        val rateUrl = getRatingLink(videoId.value)

        mockMvc.perform(patch(rateUrl, rating).asTeacher()).andExpect(status().isOk)
    }
}

