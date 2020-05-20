package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.boclips.videos.api.request.channel.ChannelStatusRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Period

class LegacyContentPartnerControllerIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var contractId: String

    @BeforeEach
    fun setUp() {
        contractId = saveContentPartnerContract(name = "hello", remittanceCurrency = "USD").id.value
    }

    @Test
    fun `post video lookup by provider id returns 200 when video exists`() {
        val contentPartner = saveContentPartner(name = "ted")
        saveVideo(
            contentProvider = "ted",
            contentProviderVideoId = "https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/"
        )

        mockMvc.perform(
            post("/v1/content-partners/${contentPartner.id.value}/videos/search")
                .content("https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/")
                .contentType(MediaType.TEXT_PLAIN)
                .asIngestor()
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `post video lookup by provider id returns 404 when video does not exist`() {
        mockMvc.perform(
            post("/v1/content-partners/ted/videos/search")
                .content("https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/")
                .contentType(MediaType.TEXT_PLAIN)
                .asIngestor()
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create a content partner and rejects an existing content partner`() {
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
                "awards": "award",
                "notes": "note one",
                "hubspotId": "123456789",
                "contentCategories": ["ANIMATION","HISTORICAL_ARCHIVE"],
                "language": "spa",
                "contractId": "$contractId"
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isConflict)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `reject creating a content partner with null elements in list fields`() {
        val listFieldNames = listOf(
            "ageRanges",
            "contentCategories",
            "contentTypes",
            "bestForTags",
            "subjects"
        )

        val makeInvalidRequest = { fieldName: String ->
            mockMvc.perform(
                post("/v1/content-partners")
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

        //nested fields

        mockMvc.perform(
            post("/v1/content-partners")
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
            post("/v1/content-partners")
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
    fun `create content partner with correct values`() {
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
                "awards": "award",
                "notes": "note one",
                "hubspotId": "123456789",
                "contractId": "${contractId}",
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
                "deliveryFrequency": "P6M",
                "marketingInformation": {
                    "status": "PROMOTED",
                    "logos": ["http://sample1.com", "http://sample2.com"],
                    "showreel": "http://sample3.com",
                    "sampleVideos": ["http://sample4.com", "http://sample5.com"]
                }
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            get("/v1/content-partners?name=TED").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("TED")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency", equalTo("USD")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].description", equalTo("This is a description")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].awards", equalTo("award")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].notes", equalTo("note one")))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].oneLineDescription",
                    equalTo("My one-line description")
                )
            )
            .andExpect(jsonPath("$._embedded.contentPartners[0].ingest.type", equalTo("MRSS")))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].ingest.urls",
                    equalTo(listOf("http://mrss.feed", "http://mrss2.feed"))
                )
            )
            .andExpect(jsonPath("$._embedded.contentPartners[0].deliveryFrequency", equalTo("P6M")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].marketingInformation.status", equalTo("PROMOTED")))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].marketingInformation.logos",
                    containsInAnyOrder("http://sample1.com", "http://sample2.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].marketingInformation.showreel",
                    equalTo("http://sample3.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].marketingInformation.sampleVideos",
                    containsInAnyOrder("http://sample4.com", "http://sample5.com")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].contentTypes",
                    containsInAnyOrder("NEWS", "INSTRUCTIONAL")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].contentCategories[*].key",
                    containsInAnyOrder("ANIMATION", "HISTORICAL_ARCHIVE")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].contentCategories[*].label",
                    containsInAnyOrder("Animation", "Historical archive")
                )
            )
            .andExpect(jsonPath("$._embedded.contentPartners[0].language.code", equalTo("spa")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].language.name", equalTo("Spanish")))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.ageRanges.ids",
                    containsInAnyOrder("early", "not-so-early")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.isTranscriptProvided",
                    equalTo(true)
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.educationalResources",
                    equalTo("This is a resource")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.curriculumAligned",
                    equalTo("This is a curriculum")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.bestForTags",
                    containsInAnyOrder("123", "345")
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0].pedagogyInformation.subjects",
                    containsInAnyOrder("subject 1", "subject 2")
                )
            )
    }

    @Test
    fun `can filter content partners by name`() {
        saveContentPartner(name = "hello")
        saveContentPartner(name = "goodbye")

        mockMvc.perform(
            get("/v1/content-partners?name=hello").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
    }

    @Test
    fun `can filter content partners by ingest types`() {
        saveContentPartner(
            name = "mrss",
            ingest = ChannelFactory.createIngestDetailsResource(
                type = IngestType.MRSS,
                urls = listOf("http://feed.me")
            )
        )
        saveContentPartner(
            name = "yt",
            ingest = ChannelFactory.createIngestDetailsResource(
                type = IngestType.YOUTUBE,
                playlistIds = listOf("http://yt.com")
            )
        )
        saveContentPartner(
            name = "manual",
            ingest = ChannelFactory.createIngestDetailsResource(type = IngestType.MANUAL)
        )

        mockMvc.perform(
            get("/v1/content-partners?ingestType=MRSS&ingestType=YOUTUBE").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", oneOf("mrss", "yt")))
            .andExpect(jsonPath("$._embedded.contentPartners[1].name", oneOf("mrss", "yt")))
    }

    @Test
    fun `can find content partner, but cannot see currency as just an API user`() {
        saveContentPartner(name = "hello", currency = "USD")

        mockMvc.perform(
            get("/v1/content-partners?name=hello").asApiUser()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency").doesNotExist())
    }

    @Test
    fun `can filter content partners by officiality`() {
        saveContentPartner(name = "cp-1", accreditedToYtChannel = "1234")
        saveContentPartner(name = "cp-2", accreditedToYtChannel = null)

        mockMvc.perform(
            get("/v1/content-partners?official=true").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].official", equalTo(true)))
    }

    @Test
    fun `can filter content partners by youtube channel`() {
        saveContentPartner(name = "cp-1", accreditedToYtChannel = "1234")
        saveContentPartner(name = "cp-2", accreditedToYtChannel = null)

        mockMvc.perform(
            get("/v1/content-partners?accreditedToYtChannelId=1234").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].official", equalTo(false)))
    }

    @Test
    fun `create content partner accredited to youtube`() {
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

        val contentPartnerUrl = mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(
            get(contentPartnerUrl).asBoclipsEmployee()
        )
            .andExpect(jsonPath("$.oneLineDescription", equalTo(oneLineDescription)))
            .andExpect(jsonPath("$.marketingInformation.status", equalTo(status.toString())))
    }

    @Test
    fun `update a content partner`() {
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

        val firstContractId = saveContentPartnerContract(name = "first contract").id
        val secondContractId = saveContentPartnerContract(name = "second contract").id

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
            post("/v1/content-partners").asBoclipsEmployee()
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
                    "$.pedagogyInformation.isTranscriptProvided",
                    equalTo(true)
                )
            )
            .andExpect(
                jsonPath(
                    "$.pedagogyInformation.educationalResources",
                    equalTo("This is a resource 3")
                )
            )
            .andExpect(
                jsonPath(
                    "$.pedagogyInformation.curriculumAligned",
                    equalTo("This is a curriculum 3")
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
    fun `bad request when updating to a non-existent contract`() {
        val updatedContent = """
            {
                "name": "ignored",
                "contractId": "i am missing"
            }
        """

        mockMvc.perform(
            patch("/v1/content-partners/123").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(updatedContent)
        )
            .andExpect(status().isBadRequest)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `get all content partners`() {
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
            post("/v1/content-partners").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(originalContent)
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/v1/content-partners").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("TED-ED")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency", equalTo("USD")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].official", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0]._links.self.href",
                    containsString("/content-partners/")
                )
            )
    }

    @Test
    fun `enables content partner for streaming`() {
        val id = saveContentPartner().id.value

        mockMvc.perform(
            patch("/v1/content-partners/$id").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                """{
                        "distributionMethods": ["STREAM"],
                        "name": "TED"
                    }"""
            )
        ).andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/content-partners/$id").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.distributionMethods", equalTo(listOf("STREAM"))))
    }

    @Test
    fun `retrieves a signed link`() {
        val sampleLink = URI("http://sample.com/").toURL()

        fakeSignedLinkProvider.setLink(sampleLink)

        val location = mockMvc.perform(
            post("/v1/content-partners/signed-upload-link").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
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
    fun `create a content partner associated to a contract`() {
        val contractId = saveContentPartnerContract(name = "a contract", remittanceCurrency = "USD").id

        val content = """
            {
                "name": "a content partner",
                "contractId": "${contractId.value}"
            }
        """

        val contentPartnerUrl = mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(get(contentPartnerUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.contractId", equalTo(contractId.value)))
            .andExpect(jsonPath("$.contractName", equalTo("a contract")))
    }

    @Nested
    inner class ChannelResourceProjections {
        @Test
        fun `boclips internal user has access to all fields`() {
            val contentPartner = saveContentPartner(
                name = "hello",
                currency = "CAD",
                awards = "this is an award",
                description = "this is a description",
                contentCategories = listOf("WITH_A_HOST"),
                hubspotId = "123456",
                notes = "this is a note",
                language = "eng",
                oneLineDescription = "This is a single-line description",
                marketingInformation = MarketingInformationRequest(
                    status = ChannelStatusRequest.HAVE_REACHED_OUT,
                    logos = listOf("http://server.com/logo.png"),
                    showreel = Specified("http://server.com/showreel.mov"),
                    sampleVideos = listOf("http://server.com/sample.mov")
                ),
                deliveryFrequency = Period.ofMonths(6),
                isTranscriptProvided = true,
                educationalResources = "this is a resource",
                curriculumAligned = "this is a curriculum",
                bestForTags = listOf("123", "456"),
                subjects = listOf("subject 1", "subject 2")
            )

            mockMvc.perform(
                get(
                    "/v1/content-partners/${contentPartner.id.value}"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", equalTo("hello")))
                .andExpect(jsonPath("$.currency").exists())
                .andExpect(jsonPath("$.awards").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.contentCategories").exists())
                .andExpect(jsonPath("$.hubspotId").exists())
                .andExpect(jsonPath("$.notes").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.marketingInformation").exists())
                .andExpect(jsonPath("$.ingest").exists())
                .andExpect(jsonPath("$.deliveryFrequency").exists())
                .andExpect(jsonPath("$.pedagogyInformation.educationalResources").exists())
                .andExpect(jsonPath("$.pedagogyInformation.curriculumAligned").exists())
                .andExpect(jsonPath("$.pedagogyInformation.bestForTags").exists())
                .andExpect(jsonPath("$.pedagogyInformation.subjects").exists())

            mockMvc.perform(
                get(
                    "/v1/content-partners?name=hello"
                ).asBoclipsEmployee()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
                .andExpect(jsonPath("$._embedded.contentPartners[0].currency").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].awards").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].description").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].contentCategories").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].hubspotId").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].notes").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].language").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].marketingInformation").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].ingest").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].deliveryFrequency").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].pedagogyInformation.educationalResources").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].pedagogyInformation.curriculumAligned").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].pedagogyInformation.bestForTags").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].pedagogyInformation.subjects").exists())
        }

        @Test
        fun `api user only has access to certain fields`() {
            val contentPartner = saveContentPartner(
                name = "hello",
                currency = "CAD",
                awards = "this is an award",
                description = "this is a description",
                contentCategories = listOf("WITH_A_HOST"),
                hubspotId = "123456",
                notes = "this is a note",
                language = "eng",
                curriculumAligned = "this is a curriculum",
                isTranscriptProvided = true,
                deliveryFrequency = Period.ofMonths(6),
                educationalResources = "this is an educational resource",
                bestForTags = listOf("123"),
                subjects = listOf("subject 1")
            )


            mockMvc.perform(
                get(
                    "/v1/content-partners/${contentPartner.id.value}"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", equalTo("hello")))
                .andExpect(jsonPath("$.awards").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.contentCategories").exists())
                .andExpect(jsonPath("$.notes").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.currency").doesNotExist())
                .andExpect(jsonPath("$.hubspotId").doesNotExist())
                .andExpect(jsonPath("$.official").doesNotExist())
                .andExpect(jsonPath("$.distributionMethods").doesNotExist())
                .andExpect(jsonPath("$.ingest").doesNotExist())
                .andExpect(jsonPath("$.deliveryFrequency").doesNotExist())
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
                    "/v1/content-partners?name=hello"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
                .andExpect(jsonPath("$._embedded.contentPartners[0].awards").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].description").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].contentCategories").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].notes").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].language").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].currency").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].hubspotId").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].official").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].distributionMethods").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].ingest").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].deliveryFrequency").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].marketingInformation").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].educationalResources").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].curriculumAligned").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].bestForTags").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].subjects").doesNotExist())
        }
    }
}