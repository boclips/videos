package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.boclips.videos.api.request.channel.ChannelStatusRequest
import com.boclips.videos.api.request.channel.ContentCategoryRequest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.oneOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Period

class LegacyContentPartnerControllerIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var contractId: String

    @BeforeEach
    fun setUp() {
        contractId = saveContract(name = "hello", remittanceCurrency = "USD").id.value
    }

    @Test
    fun `can filter content partners by name`() {
        saveChannel(name = "hello")
        saveChannel(name = "goodbye")

        mockMvc.perform(
            get("/v1/content-partners?name=hello").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
    }

    @Test
    fun `can filter content partners by ingest types`() {
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
            get("/v1/content-partners?ingestType=MRSS&ingestType=YOUTUBE").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(2)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", oneOf("mrss", "yt")))
            .andExpect(jsonPath("$._embedded.contentPartners[1].name", oneOf("mrss", "yt")))
    }

    @Test
    fun `can find content partner, but cannot see currency as just an API user`() {
        saveChannel(name = "hello", currency = "USD")

        mockMvc.perform(
            get("/v1/content-partners?name=hello").asApiUser()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("hello")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency").doesNotExist())
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
        saveChannel(name = "TED-ED", ageRanges = listOf("early-years"), contractId = contractId)

        mockMvc.perform(get("/v1/content-partners").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("TED-ED")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].currency", equalTo("USD")))
            .andExpect(
                jsonPath(
                    "$._embedded.contentPartners[0]._links.self.href",
                    containsString("/content-partners/")
                )
            )
    }
    @Nested
    inner class ContentPartnerResourceProjections {
        @Test
        fun `boclips internal user has access to all fields`() {
            val contentPartner = saveChannel(
                name = "hello",
                currency = "CAD",
                awards = "this is an award",
                description = "this is a description",
                contentCategories = listOf(ContentCategoryRequest.WITH_A_HOST),
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
            val contentPartner = saveChannel(
                name = "hello",
                currency = "CAD",
                awards = "this is an award",
                description = "this is a description",
                contentCategories = listOf(ContentCategoryRequest.WITH_A_HOST),
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
