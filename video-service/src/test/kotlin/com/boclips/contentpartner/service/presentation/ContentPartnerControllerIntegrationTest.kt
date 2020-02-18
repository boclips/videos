package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerMarketingRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerStatusRequest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentPartnerControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `post video lookup by provider id returns 200 when video exists`() {
        val contentPartner = saveContentPartner(name = "ted")
        saveVideo(
            contentProvider = "ted",
            contentProviderVideoId = "https://www.newsy.com/stories/u-s-announces-new-rules-for-migrant-family-detentions/"
        )

        mockMvc.perform(
            post("/v1/content-partners/${contentPartner.contentPartnerId.value}/videos/search")
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
    fun `creates content partner and rejects an existing content partner`() {
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
                "language": "spa"
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(content)
        )
            .andExpect(status().isConflict)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `creates content partner with correct values`() {
        createAgeRange(AgeRangeRequest(id = "early", min = 3, max = 5, label = "3-5"))
        createAgeRange(AgeRangeRequest(id = "not-so-early", min = 5, max = 7, label = "3-7"))

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
                "contentTypes": ["NEWS","INSTRUCTIONAL"],
                "ageRanges": ["early", "not-so-early"],
                "isTranscriptProvided": true,
                "educationalResources": "This is a resource"
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(content)
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
                    "$._embedded.contentPartners[0].ageRange.ids",
                    containsInAnyOrder("early", "not-so-early")
                )
            )
            .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange.min", equalTo(3)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange.max", equalTo(7)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].isTranscriptProvided", equalTo(true)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].educationalResources", equalTo("This is a resource")))
    }

    @Test
    fun `can filter content partners by name`() {
        saveContentPartner(name = "hello", currency = "CAD")
        saveContentPartner(name = "goodbye")

        mockMvc.perform(
            get("/v1/content-partners?name=hello").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency", equalTo("CAD")))
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
        val status = ContentPartnerStatusRequest.NeedsContent

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
                "marketingInformation": {"status": "$status"}
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
    fun `updating a content partner`() {
        createAgeRange(AgeRangeRequest(id = "early-years", min = 10, max = 15, label = "10-15"))
        createAgeRange(AgeRangeRequest(id = "late-years", label = "123", min = 50, max = 60))

        val oneLineDescription = "My one line descripTION!"
        val status = ContentPartnerStatusRequest.WaitingForIngest
        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "currency": "USD",
                "ageRanges": ["early-years"]
            }
        """
        val updatedContent = """
            {
                "searchable": false,
                "name": "TED",
                "currency": "GBP",
                "ageRanges": ["late-years"],
                "oneLineDescription": "$oneLineDescription",
                "marketingInformation": {"status": "$status"}
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
            .andExpect(jsonPath("$.ageRange.min", equalTo(50)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(60)))
            .andExpect(jsonPath("$.oneLineDescription", equalTo(oneLineDescription)))
            .andExpect(jsonPath("$.marketingInformation.status", equalTo(status.toString())))
            .andExpect(jsonPath("$._links.self.href", equalTo(cpUrl)))
    }

    @Test
    fun `get all content partners`() {
        createAgeRange(AgeRangeRequest(id = "early-years", min = 10, max = 15, label = "10-15"))

        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "currency": "USD",
                "ageRanges": ["early-years"]
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
        val id = saveContentPartner().contentPartnerId.value

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

    @Nested
    inner class ContentPartnerResourceProjections {
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
                marketingInformation = ContentPartnerMarketingRequest(
                    status = ContentPartnerStatusRequest.HaveReachedOut
                ),
                isTranscriptProvided = true,
                educationalResources = "this is a resource"
            )

            mockMvc.perform(
                get(
                    "/v1/content-partners/${contentPartner.contentPartnerId.value}"
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
                .andExpect(jsonPath("$.ageRange").exists())
                .andExpect(jsonPath("$.marketingInformation").exists())
                .andExpect(jsonPath("$.educationalResources").exists())

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
                .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].marketingInformation").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].educationalResources").exists())
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
                language = "eng"
            )


            mockMvc.perform(
                get(
                    "/v1/content-partners/${contentPartner.contentPartnerId.value}"
                ).asApiUser()
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", equalTo("hello")))
                .andExpect(jsonPath("$.awards").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.contentCategories").exists())
                .andExpect(jsonPath("$.notes").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.ageRange").exists())
                .andExpect(jsonPath("$.currency").doesNotExist())
                .andExpect(jsonPath("$.hubspotId").doesNotExist())
                .andExpect(jsonPath("$.official").doesNotExist())
                .andExpect(jsonPath("$.distributionMethods").doesNotExist())
                .andExpect(jsonPath("$.marketingInformation").doesNotExist())

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
                .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange").exists())
                .andExpect(jsonPath("$._embedded.contentPartners[0].currency").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].hubspotId").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].official").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].distributionMethods").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].marketingInformation").doesNotExist())
                .andExpect(jsonPath("$._embedded.contentPartners[0].educationalResources").doesNotExist())
        }
    }
}
