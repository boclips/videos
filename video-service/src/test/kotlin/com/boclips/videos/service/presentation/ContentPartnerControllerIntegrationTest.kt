package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentPartnerControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `video lookup by provider id returns 200 when video exists`() {
        val contentPartner = saveContentPartner(name = "ted")
        saveVideo(contentProvider = "ted", contentProviderVideoId = "abc")

        mockMvc.perform(head("/v1/content-partners/${contentPartner.contentPartnerId.value}/videos/abc").asIngestor())
            .andExpect(status().isOk)
    }

    @Test
    fun `video lookup by provider id returns 404 when video does not exist`() {
        mockMvc.perform(head("/v1/content-partners/ted/videos/xyz").asIngestor())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create a content partner`() {
        val content = """
            {
                "searchable": false,
                "name": "TED",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    }
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
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
    fun `can filter content partners by officiality`() {
        saveContentPartner(accreditedToYtChannel = "1234")
        saveContentPartner(accreditedToYtChannel = null)

        mockMvc.perform(
            get("/v1/content-partners?official=true").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].official", equalTo(true)))
    }

    @Test
    fun `can filter content partners by youtube channel`() {
        saveContentPartner(accreditedToYtChannel = "1234")
        saveContentPartner(accreditedToYtChannel = null)

        mockMvc.perform(
            get("/v1/content-partners?accreditedToYtChannelId=1234").asBoclipsEmployee()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].official", equalTo(false)))
    }

    @Test
    fun `create content partner accredited to youtube`() {
        val content = """
            {
                "searchable": false,
                "name": "Youtube Channel",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    },
                "accreditedToYtChannelId": "some-yt-channel-id"
            }
        """

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                content
            )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
    }

    @Test
    fun `updating a content partner`() {
        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "ageRange":
                    {
                        "min": 10,
                        "max": 19
                    }
            }
        """
        val updatedContent = """
            {
                "searchable": false,
                "name": "TED",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    }
            }
        """

        val cpUrl = mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(originalContent)
        )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(put(cpUrl).asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(updatedContent))
            .andExpect(status().isNoContent)

        mockMvc.perform(get(cpUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("TED")))
            .andExpect(jsonPath("$.ageRange.min", equalTo(11)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(18)))
    }

    @Test
    fun `get all content partners`() {
        val originalContent = """
            {
                "searchable": false,
                "name": "TED-ED",
                "ageRange":
                    {
                        "min": 10,
                        "max": 19
                    }
            }"""

        mockMvc.perform(
            post("/v1/content-partners").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(originalContent)
        )

        mockMvc.perform(get("/v1/content-partners").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentPartners[0].id").exists())
            .andExpect(jsonPath("$._embedded.contentPartners[0].name", equalTo("TED-ED")))
            .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange.min", equalTo(10)))
            .andExpect(jsonPath("$._embedded.contentPartners[0].ageRange.max", equalTo(19)))
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
        saveVideo(contentProviderId = "deadb33d1225df4825e8b8f6")

        mockMvc.perform(
            put("/v1/content-partners/deadb33d1225df4825e8b8f6").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(
                """{
                        "distributionMethods": ["STREAM"],
                        "name": "TED",
                        "ageRange":
                        {
                            "min": 11,
                            "max": 18
                        }
                    }"""
            )
        ).andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/content-partners/deadb33d1225df4825e8b8f6").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.distributionMethods", equalTo(listOf("STREAM"))))
    }
}
