package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.request.channel.ChannelStatusRequest
import com.boclips.videos.api.request.channel.ContentCategoryRequest
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.oneOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Period

class ChannelControllerIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var contractId: String

    @BeforeEach
    fun setUp() {
        contractId = saveContract(name = "hello", remittanceCurrency = "USD").id.value
    }

    @Test
    fun `post video lookup by provider id returns 200 when video exists`() {
        val channel = saveChannel(name = "ted")
        saveVideo(
            contentProvider = "ted",
            contentProviderVideoId = "https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/"
        )

        mockMvc.perform(
            post("/v1/channels/${channel.id.value}/videos/search")
                .content("https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/")
                .contentType(MediaType.TEXT_PLAIN)
                .asIngestor()
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `post video lookup by provider id returns 404 when video does not exist`() {
        mockMvc.perform(
            post("/v1/channels/ted/videos/search")
                .content("https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/")
                .contentType(MediaType.TEXT_PLAIN)
                .asIngestor()
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create a channel and rejects an existing channel`() {
        val content = """
            {
                "searchable": false,
                "name": "TED",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    },
                "currency": "USD",
                "description": "This is a description",
                "notes": "note one",
                "contentCategories": ["ANIMATION","HISTORICAL_ARCHIVE"],
                "language": "spa",
                "contractId": "$contractId"
            }
        """

        mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isConflict)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `reject creating a channel with null elements in list fields`() {
        val listFieldNames = listOf(
            "ageRanges",
            "contentCategories",
            "contentTypes",
            "bestForTags",
            "subjects"
        )

        val makeInvalidRequest = { fieldName: String ->
            mockMvc.perform(
                post("/v1/channels")
                    .asBoclipsEmployee()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "name": "random name",
                                "$fieldName": [null]
                            }
                        """.trimIndent()
                    )
            )
                .andExpect(status().isBadRequest)
        }

        listFieldNames.map { makeInvalidRequest(it) }

        mockMvc.perform(
            post("/v1/channels")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "name": "random name",
                            "marketingInformation": {
                                "logos": [null]
                            }
                        }
                    """
                )
        )
            .andExpect(status().isBadRequest)

        mockMvc.perform(
            post("/v1/channels")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "name": "random name",
                            "marketingInformation": {
                                "sampleVideos": [null]
                            }
                        }
                    """
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create channel with correct values`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early",
                min = 3,
                max = 5,
                label = "3-5"
            )
        )
        createAgeRange(
            AgeRangeRequest(
                id = "not-so-early",
                min = 5,
                max = 7,
                label = "3-7"
            )
        )

        val content = """
            {
                "searchable": false,
                "name": "TED",
                "description": "This is a description",
                "notes": "note one",
                "contractId": "$contractId",
                "contentCategories": ["ANIMATION","HISTORICAL_ARCHIVE"],
                "language": "spa",
                "contentTypes": ["NEWS","INSTRUCTIONAL"],
                "ageRanges": ["early", "not-so-early"],
                "isTranscriptProvided": true,
                "educationalResources": "This is a resource",
                "curriculumAligned": "This is a curriculum",
                "bestForTags": ["123", "345"],
                "subjects": ["subject 1", "subject 2"],
                "oneLineDescription": "My one-line description",
                "ingest": {
                    "type": "MRSS",
                    "urls": ["http://mrss.feed", "http://mrss2.feed"]
                },
                "marketingInformation": {
                    "status": "PROMOTED",
                    "logos": ["http://sample1.com", "http://sample2.com"],
                    "showreel": "http://sample3.com",
                    "sampleVideos": ["http://sample4.com", "http://sample5.com"]
                }
            }
        """

        mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            get("/v1/channels?name=TED").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.channels[0].id").exists())
            .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("TED")))
            .andExpect(jsonPath("$._embedded.channels[0].currency", equalTo("USD")))
            .andExpect(jsonPath("$._embedded.channels[0].description", equalTo("This is a description")))
            .andExpect(jsonPath("$._embedded.channels[0].notes", equalTo("note one")))
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].oneLineDescription",
                    equalTo("My one-line description")
                )
            )
            .andExpect(jsonPath("$._embedded.channels[0].ingest.type", equalTo("MRSS")))
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].ingest.urls",
                    equalTo(listOf("http://mrss.feed", "http://mrss2.feed"))
                )
            )
            .andExpect(jsonPath("$._embedded.channels[0].marketingInformation.status", equalTo("PROMOTED")))
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].marketingInformation.logos",
                    containsInAnyOrder("http://sample1.com", "http://sample2.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].marketingInformation.showreel",
                    equalTo("http://sample3.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].marketingInformation.sampleVideos",
                    containsInAnyOrder("http://sample4.com", "http://sample5.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].contentTypes",
                    containsInAnyOrder("NEWS", "INSTRUCTIONAL")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].contentCategories[*].key",
                    containsInAnyOrder("ANIMATION", "HISTORICAL_ARCHIVE")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].contentCategories[*].label",
                    containsInAnyOrder("Animation", "Historical archive")
                )
            )
            .andExpect(jsonPath("$._embedded.channels[0].language.code", equalTo("spa")))
            .andExpect(jsonPath("$._embedded.channels[0].language.name", equalTo("Spanish")))
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].pedagogyInformation.ageRanges.ids",
                    containsInAnyOrder("early", "not-so-early")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].pedagogyInformation.bestForTags",
                    containsInAnyOrder("123", "345")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0].pedagogyInformation.subjects",
                    containsInAnyOrder("subject 1", "subject 2")
                )
            )
    }

    @Test
    fun `returns only ID and name for every channel when list projection is requested`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early",
                min = 3,
                max = 5,
                label = "3-5"
            )
        )
        createAgeRange(
            AgeRangeRequest(
                id = "not-so-early",
                min = 5,
                max = 7,
                label = "3-7"
            )
        )

        val content = """
            {
                "searchable": false,
                "name": "TED",
                "description": "This is a description",
                "notes": "note one",
                "contractId": "$contractId",
                "contentCategories": ["ANIMATION","HISTORICAL_ARCHIVE"],
                "language": "spa",
                "contentTypes": ["NEWS","INSTRUCTIONAL"],
                "ageRanges": ["early", "not-so-early"],
                "isTranscriptProvided": true,
                "educationalResources": "This is a resource",
                "curriculumAligned": "This is a curriculum",
                "bestForTags": ["123", "345"],
                "subjects": ["subject 1", "subject 2"],
                "oneLineDescription": "My one-line description",
                "ingest": {
                    "type": "MRSS",
                    "urls": ["http://mrss.feed", "http://mrss2.feed"]
                },
                "marketingInformation": {
                    "status": "PROMOTED",
                    "logos": ["http://sample1.com", "http://sample2.com"],
                    "showreel": "http://sample3.com",
                    "sampleVideos": ["http://sample4.com", "http://sample5.com"]
                }
            }
        """

        mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            get("/v1/channels?name=TED&projection=list").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.channels[0].id").exists())
            .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("TED")))
            .andExpect(jsonPath("$._embedded.channels[0].currency").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].description").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].notes").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].oneLineDescription").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].ingest").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].marketingInformation").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].contentTypes").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].contentCategories").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].language").doesNotExist())
            .andExpect(jsonPath("$._embedded.channels[0].pedagogyInformation").doesNotExist())
    }

    @Test
    fun `can filter channels by name`() {
        saveChannel(name = "hello")
        saveChannel(name = "goodbye")

        mockMvc.perform(
            get("/v1/channels?name=hello").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.channels[0].id").exists())
            .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("hello")))
    }

    @Test
    fun `can filter channels by ingest types`() {
        saveChannel(
            name = "mrss",
            ingest = ChannelFactory.createIngestDetailsResource(
                type = IngestType.MRSS,
                urls = listOf("http://feed.me")
            )
        )
        saveChannel(
            name = "yt",
            ingest = ChannelFactory.createIngestDetailsResource(
                type = IngestType.YOUTUBE,
                playlistIds = listOf("http://yt.com")
            )
        )
        saveChannel(
            name = "manual",
            ingest = ChannelFactory.createIngestDetailsResource(type = IngestType.MANUAL)
        )

        mockMvc.perform(
            get("/v1/channels?ingestType=MRSS&ingestType=YOUTUBE").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.channels[0].name", oneOf("mrss", "yt")))
            .andExpect(jsonPath("$._embedded.channels[1].name", oneOf("mrss", "yt")))
    }

    @Test
    fun `can find channel, but cannot see currency as just an API user`() {
        saveChannel(name = "hello", currency = "USD")

        mockMvc.perform(
            get("/v1/channels?name=hello").asApiUser()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.channels[0].id").exists())
            .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("hello")))
            .andExpect(jsonPath("$._embedded.channels[0].currency").doesNotExist())
    }

    @Test
    fun `create channel accredited to youtube`() {
        val oneLineDescription = "My one-line description"
        val status = ChannelStatusRequest.NEEDS_CONTENT

        val content = """
            {
                "searchable": false,
                "name": "Youtube Channel",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    },
                "accreditedToYtChannelId": "some-yt-channel-id",
                "oneLineDescription": "$oneLineDescription",
                "marketingInformation": {
                    "status": "$status"
                },
                "contractId": "$contractId"
            }
        """

        val channelUrl = mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(
            get(channelUrl).asBoclipsEmployee()
        )
            .andExpect(jsonPath("$.oneLineDescription", equalTo(oneLineDescription)))
            .andExpect(jsonPath("$.marketingInformation.status", equalTo(status.toString())))
    }

    @Test
    fun `update a channel`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early-years",
                min = 10,
                max = 15,
                label = "10-15"
            )
        )
        createAgeRange(
            AgeRangeRequest(
                id = "late-years",
                label = "123",
                min = 50,
                max = 60
            )
        )

        val firstContractId = saveContract(name = "first contract").id
        val secondContractId = saveContract(name = "second contract").id

        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "currency": "USD",
                "ageRanges": ["early-years"],
                "oneLineDescription": "My one line descripTION",
                "marketingInformation": {
                    "status": "NEEDS_INTRODUCTION",
                    "logos": ["http://server.com/logo.png", "http://server.com/logo2.png"],
                    "showreel": "http://server.com/myshowreel.mov",
                    "sampleVideos": ["http://server.com/sample1.mp4", "http://server.com/sample2.mp4"]
                },
                "curriculumAligned": "This is a curriculum 1",
                "educationalResources": "This is a resource 2",
                "isTranscriptProvided": false,
                "bestForTags": ["222","333"],
                "subjects": ["subject 1", "subject 2"],
                "contractId": "${firstContractId.value}"
            }
        """
        val updatedContent = """
            {
                "searchable": false,
                "name": "TED",
                "ageRanges": ["late-years"],
                "curriculumAligned": "This is a curriculum 3",
                "oneLineDescription": "My new one line descripTION",
                "marketingInformation": {
                    "status": "WAITING_FOR_INGEST",
                    "logos": ["http://server.com/newlogo.png", "http://server.com/newlogo2.png"],
                    "showreel": "http://server.com/newmyshowreel.mov",
                    "sampleVideos": ["http://server.com/newsample1.mp4", "http://server.com/newsample2.mp4"]
                },
                "educationalResources": "This is a resource 3",
                "isTranscriptProvided": true,
                "bestForTags": ["123", "345"],
                "subjects": ["sub 1", "sub 2"],
                "contractId": "${secondContractId.value}"
            }
        """

        val cpUrl = mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(originalContent)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(
            patch(cpUrl).asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(updatedContent)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get(cpUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("TED")))
            .andExpect(jsonPath("$.currency", equalTo("GBP")))
            .andExpect(
                jsonPath(
                    "$.pedagogyInformation.ageRanges.ids",
                    containsInAnyOrder("late-years")
                )
            )
            .andExpect(
                jsonPath(
                    "$.pedagogyInformation.bestForTags",
                    containsInAnyOrder("123", "345")
                )
            )
            .andExpect(
                jsonPath(
                    "$.pedagogyInformation.subjects",
                    containsInAnyOrder("sub 1", "sub 2")
                )
            )
            .andExpect(jsonPath("$.oneLineDescription", equalTo("My new one line descripTION")))
            .andExpect(jsonPath("$.marketingInformation.status", equalTo("WAITING_FOR_INGEST")))
            .andExpect(
                jsonPath(
                    "$.marketingInformation.logos",
                    containsInAnyOrder("http://server.com/newlogo.png", "http://server.com/newlogo2.png")
                )
            )
            .andExpect(jsonPath("$.marketingInformation.showreel", equalTo("http://server.com/newmyshowreel.mov")))
            .andExpect(
                jsonPath(
                    "$.marketingInformation.sampleVideos",
                    containsInAnyOrder("http://server.com/newsample1.mp4", "http://server.com/newsample2.mp4")
                )
            )
            .andExpect(jsonPath("$.contractId", equalTo(secondContractId.value)))
            .andExpect(jsonPath("$.contractName", equalTo("second contract")))
            .andExpect(jsonPath("$._links.self.href", equalTo(cpUrl)))
    }

    @Test
    fun `can update categories in channel`() {
        addCategory(CategoryFactory.sample(code = "A", description = "Law"))
        addCategory(CategoryFactory.sample(code = "BC", description = "Interior Design"))

        val channelId = saveChannel(name = "Test channel").id.value
        val channelUpdateRequest = """
            {
            "name": "Test channel",
            "categories": ["A", "BC"]
            }
        """

        mockMvc.perform(
            patch("/v1/channels/$channelId")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(channelUpdateRequest)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/v1/channels/$channelId")
                .asBoclipsEmployee()
        )
            .andExpect(jsonPath("$.taxonomy.categories", hasSize<Int>(2)))
            .andExpect(jsonPath("$.taxonomy.categories[*].codeValue", containsInAnyOrder("A", "BC")))
            .andExpect(jsonPath("$.taxonomy.categories[*].description", containsInAnyOrder("Law", "Interior Design")))
    }

    @Test
    fun `can update videoLevelTagging`() {
        addCategory(CategoryFactory.sample(code = "ABC", description = "Law"))
        val channel = saveChannel(
            name = "Test channel",
            categories = listOf("ABC"),
            videoLevelTagging = false
        )
        val channelId = channel.id.value

        val channelUpdateRequest = """
            {
            "name": "Test channel",
            "requiresVideoLevelTagging": true
            }
        """

        mockMvc.perform(
            patch("/v1/channels/$channelId")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(channelUpdateRequest)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/v1/channels/$channelId")
                .asBoclipsEmployee()
        )
            .andExpect(jsonPath("$.taxonomy.categories", equalTo(null)))
            .andExpect(jsonPath("$.taxonomy.requiresVideoLevelTagging", equalTo(true)))
    }

    @Test
    fun `updating videoLevelTagging in channel triggers ChannelUpdated event`() {
        addCategory(CategoryFactory.sample(code = "ABC", description = "Law"))
        val channel = saveChannel(
            name = "Test channel",
            categories = listOf("ABC"),
            videoLevelTagging = false
        )
        val channelId = channel.id.value

        val channelUpdateRequest = """
            {
            "name": "Test channel",
            "requiresVideoLevelTagging": true
            }
        """

        mockMvc.perform(
            patch("/v1/channels/$channelId")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(channelUpdateRequest)
        )
            .andExpect(status().isNoContent)

        assertThat(fakeEventBus.getEventsOfType(ContentPartnerUpdated::class.java)).hasSize(1)
        assertThat(fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java).contentPartner.name).isEqualTo("Test channel")
        val categoriesFromEvent =
            fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java).contentPartner.categories
        assertThat(categoriesFromEvent).hasSize(0)
    }

    @Test
    fun `updating categories in channel triggers ChannelUpdated event`() {
        addCategory(CategoryFactory.sample(code = "A", description = "Law"))
        addCategory(CategoryFactory.sample(code = "BC", description = "Interior Design"))

        val channelId = saveChannel(name = "Test channel").id.value
        val channelUpdateRequest = """
            {
            "name": "Test channel",
            "categories": ["A", "BC"]
            }
        """

        mockMvc.perform(
            patch("/v1/channels/$channelId")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(channelUpdateRequest)
        )
            .andExpect(status().isNoContent)

        assertThat(fakeEventBus.getEventsOfType(ContentPartnerUpdated::class.java)).hasSize(1)
        assertThat(fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java).contentPartner.name).isEqualTo("Test channel")
        val categoriesFromEvent =
            fakeEventBus.getEventOfType(ContentPartnerUpdated::class.java).contentPartner.categories
        assertThat(categoriesFromEvent).hasSize(2)
        val categoryA = categoriesFromEvent.first { it.code == "A" }
        val categoryBC = categoriesFromEvent.first { it.code == "BC" }
        assertThat(categoryA.code).isEqualTo("A")
        assertThat(categoryA.description).isEqualTo("Law")
        assertThat(categoryA.ancestors).isEmpty()
        assertThat(categoryBC.code).isEqualTo("BC")
        assertThat(categoryBC.description).isEqualTo("Interior Design")
        assertThat(categoryBC.ancestors).containsOnly("B")
    }

    @Test
    fun `bad request when updating to a non-existent contract`() {
        val updatedContent = """
            {
                "name": "ignored",
                "contractId": "i am missing"
            }
        """

        mockMvc.perform(
            patch("/v1/channels/123").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(updatedContent)
        )
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `get all channels`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early-years",
                min = 10,
                max = 15,
                label = "10-15"
            )
        )

        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "currency": "USD",
                "ageRanges": ["early-years"],
                "contractId": "$contractId"
            }"""

        mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(originalContent)
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/v1/channels").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.channels[0].id").exists())
            .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("TED-ED")))
            .andExpect(jsonPath("$._embedded.channels[0].currency", equalTo("USD")))
            .andExpect(
                jsonPath(
                    "$._embedded.channels[0]._links.self.href",
                    containsString("/channels/")
                )
            )
    }

    @Test
    fun `enables channel for streaming`() {
        val id = saveChannel().id.value

        mockMvc.perform(
            patch("/v1/channels/$id").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                """{
                        "distributionMethods": ["STREAM"],
                        "name": "TED"
                    }"""
            )
        ).andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/channels/$id").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.distributionMethods", equalTo(listOf("STREAM"))))
    }

    @Test
    fun `retrieves a signed link`() {
        val sampleLink = URI("http://sample.com/").toURL()

        fakeSignedLinkProvider.setLink(sampleLink)

        val location = mockMvc.perform(
            post("/v1/channels/signed-upload-link").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                    |   "filename": "myImage.png"
                    |}
                """.trimMargin()
                )
        )
            .andExpect(status().isNoContent)
            .andExpect(header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        assertThat(sampleLink.toString()).isEqualTo(location)
    }

    @Test
    fun `create a channel associated to a contract`() {
        val contractId = saveContract(name = "a contract", remittanceCurrency = "USD").id

        val content = """
            {
                "name": "a channel",
                "contractId": "${contractId.value}"
            }
        """

        val channelUrl = mockMvc.perform(
            post("/v1/channels").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(get(channelUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.contractId", equalTo(contractId.value)))
            .andExpect(jsonPath("$.contractName", equalTo("a contract")))
    }

    @Nested
    inner class ChannelResourceProjections {
        @Test
        fun `boclips internal user has access to all fields`() {
            val channel = saveChannel(
                name = "hello",
                currency = "CAD",
                description = "this is a description",
                contentCategories = listOf(ContentCategoryRequest.WITH_A_HOST),
                notes = "this is a note",
                language = "eng",
                oneLineDescription = "This is a single-line description",
                marketingInformation = MarketingInformationRequest(
                    status = ChannelStatusRequest.HAVE_REACHED_OUT,
                    logos = listOf("http://server.com/logo.png"),
                    showreel = Specified("http://server.com/showreel.mov"),
                    sampleVideos = listOf("http://server.com/sample.mov")
                ),
                bestForTags = listOf("123", "456"),
                subjects = listOf("subject 1", "subject 2")
            )

            mockMvc.perform(
                get(
                    "/v1/channels/${channel.id.value}"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", equalTo("hello")))
                .andExpect(jsonPath("$.currency").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.contentCategories").exists())
                .andExpect(jsonPath("$.notes").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.marketingInformation").exists())
                .andExpect(jsonPath("$.ingest").exists())
                .andExpect(jsonPath("$.pedagogyInformation.bestForTags").exists())
                .andExpect(jsonPath("$.pedagogyInformation.subjects").exists())

            mockMvc.perform(
                get(
                    "/v1/channels?name=hello"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.channels[0].id").exists())
                .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("hello")))
                .andExpect(jsonPath("$._embedded.channels[0].currency").exists())
                .andExpect(jsonPath("$._embedded.channels[0].description").exists())
                .andExpect(jsonPath("$._embedded.channels[0].contentCategories").exists())
                .andExpect(jsonPath("$._embedded.channels[0].notes").exists())
                .andExpect(jsonPath("$._embedded.channels[0].language").exists())
                .andExpect(jsonPath("$._embedded.channels[0].marketingInformation").exists())
                .andExpect(jsonPath("$._embedded.channels[0].ingest").exists())
                .andExpect(jsonPath("$._embedded.channels[0].pedagogyInformation.bestForTags").exists())
                .andExpect(jsonPath("$._embedded.channels[0].pedagogyInformation.subjects").exists())
        }

        @Test
        fun `api user only has access to certain fields`() {
            val channel = saveChannel(
                name = "hello",
                currency = "CAD",
                description = "this is a description",
                contentCategories = listOf(ContentCategoryRequest.WITH_A_HOST),
                notes = "this is a note",
                language = "eng",
                curriculumAligned = "this is a curriculum",
                isTranscriptProvided = true,
                educationalResources = "this is an educational resource",
                bestForTags = listOf("123"),
                subjects = listOf("subject 1")
            )

            mockMvc.perform(
                get(
                    "/v1/channels/${channel.id.value}"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", equalTo("hello")))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.contentCategories").exists())
                .andExpect(jsonPath("$.notes").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.currency").doesNotExist())
                .andExpect(jsonPath("$.official").doesNotExist())
                .andExpect(jsonPath("$.distributionMethods").doesNotExist())
                .andExpect(jsonPath("$.ingest").doesNotExist())
                .andExpect(jsonPath("$.marketingInformation").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation.curriculumAligned").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation.educationalResources").doesNotExist())
                .andExpect(jsonPath("$.marketingInformation").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation.curriculumAligned").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation.bestForTags").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation.subjects").doesNotExist())
                .andExpect(jsonPath("$.pedagogyInformation").doesNotExist())

            mockMvc.perform(
                get(
                    "/v1/channels?name=hello"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.channels[0].id").exists())
                .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("hello")))
                .andExpect(jsonPath("$._embedded.channels[0].description").exists())
                .andExpect(jsonPath("$._embedded.channels[0].contentCategories").exists())
                .andExpect(jsonPath("$._embedded.channels[0].notes").exists())
                .andExpect(jsonPath("$._embedded.channels[0].language").exists())
                .andExpect(jsonPath("$._embedded.channels[0].currency").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].official").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].distributionMethods").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].ingest").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].marketingInformation").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].educationalResources").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].curriculumAligned").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].bestForTags").doesNotExist())
                .andExpect(jsonPath("$._embedded.channels[0].subjects").doesNotExist())
        }
    }

    @Nested
    inner class VideoExists {
        @Test
        fun `video lookup by provider id returns 200 when video exists`() {
            val contentPartner = saveChannel(name = "ted")
            saveVideo(contentProvider = "ted", contentProviderVideoId = "abc")

            mockMvc.perform(
                MockMvcRequestBuilders.head("/v1/channels/${contentPartner.id.value}/videos/abc")
                    .asIngestor()
            )
                .andExpect(status().isOk)
        }

        @Test
        fun `video lookup by provider id returns 404 when video does not exist`() {
            mockMvc.perform(MockMvcRequestBuilders.head("/v1/channels/ted/videos/xyz").asIngestor())
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class GetChannels {

        @Test
        fun `can sort by Category ASC`() {
            addCategory(Category(null, "catA", CategoryCode("A")))
            addCategory(Category(null, "catB", CategoryCode("B")))
            addCategory(Category(null, "catC", CategoryCode("C")))
            addCategory(Category(null, "catD", CategoryCode("D")))

            saveChannel(name = "Channel 1, untagged, needs video level tagging", videoLevelTagging = true)
            saveChannel(name = "Channel 2, tagged", categories = listOf("B", "D"))
            saveChannel(name = "Channel 3, tagged", categories = listOf("C", "A"))
            saveChannel(name = "Channel 4 - untagged, no video level tagging needed", categories = emptyList())

            mockMvc.perform(
                get(
                    "/v1/channels?sort_by=CATEGORIES_ASC"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.channels", hasSize<String>(4)))
                .andExpect(
                    jsonPath(
                        "$._embedded.channels[0].name",
                        equalTo("Channel 4 - untagged, no video level tagging needed")
                    )
                )
                .andExpect(
                    jsonPath(
                        "$._embedded.channels[1].name",
                        equalTo("Channel 1, untagged, needs video level tagging")
                    )
                )
                .andExpect(jsonPath("$._embedded.channels[2].name", equalTo("Channel 3, tagged")))
                .andExpect(jsonPath("$._embedded.channels[3].name", equalTo("Channel 2, tagged")))
        }

        @Test
        fun `can handle paginated requests`() {
            saveChannel(name = "Channel 1")
            saveChannel(name = "Channel 2")
            saveChannel(name = "Channel 3")
            saveChannel(name = "Channel 4")

            mockMvc.perform(
                get(
                    "/v1/channels?page=1&size=2"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.channels", hasSize<String>(2)))
                .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("Channel 3")))
                .andExpect(jsonPath("$._embedded.channels[1].name", equalTo("Channel 4")))
                .andExpect(jsonPath("$.page.size", equalTo(2)))
                .andExpect(jsonPath("$.page.totalElements", equalTo(4)))
                .andExpect(jsonPath("$.page.totalPages", equalTo(2)))
                .andExpect(jsonPath("$.page.number", equalTo(1)))
        }

        @Test
        fun `returns all records when no pagination is specified`() {
            saveChannel(name = "Channel 1")
            saveChannel(name = "Channel 2")
            saveChannel(name = "Channel 3")
            saveChannel(name = "Channel 4")

            mockMvc.perform(
                get(
                    "/v1/channels"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.channels", hasSize<String>(4)))
                .andExpect(jsonPath("$._embedded.channels[0].name", equalTo("Channel 1")))
                .andExpect(jsonPath("$._embedded.channels[1].name", equalTo("Channel 2")))
                .andExpect(jsonPath("$._embedded.channels[2].name", equalTo("Channel 3")))
                .andExpect(jsonPath("$._embedded.channels[3].name", equalTo("Channel 4")))
                .andExpect(jsonPath("$.page.size", equalTo(4)))
                .andExpect(jsonPath("$.page.totalElements", equalTo(4)))
                .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
                .andExpect(jsonPath("$.page.number", equalTo(0)))
        }

        @Test
        fun `Page information is not available via the public API`() {
            mockMvc.perform(
                get(
                    "/v1/channels"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.page").doesNotExist())
        }
    }
}
