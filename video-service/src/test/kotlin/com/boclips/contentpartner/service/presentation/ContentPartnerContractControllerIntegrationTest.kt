package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentPartnerContractControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `creates contract with correct values and fetches it`() {
        val contractUrl = mockMvc.perform(
            post("/v1/content-partner-contracts").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "contentPartnerName": "Related Content Partner",
                            "contractDocument": "http://server.com/oranges.png",
                            "contractDates": {
                                "start": "2010-12-31", 
                                "end": "2011-01-31"
                            },
                            "daysBeforeTerminationWarning": 30,
                            "yearsForMaximumLicense": 4,
                            "daysForSellOffPeriod": 12,
                            "royaltySplit": {
                                "download": 19.333333,
                                "streaming": 50
                            },
                            "minimumPriceDescription": "Minimum prices are cool",
                            "remittanceCurrency": "GBP",
                            "restrictions": {
                                "clientFacing": ["Restriction 1", "Restriction 2"],
                                "territory": "Austria",
                                "licensing": "123456789",
                                "editing": "no removal of logo",
                                "marketing": "marketing info",
                                "companies": "company one",
                                "payout": "payout",
                                "other": "other"
                            }
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(
                get(contractUrl).asBoclipsEmployee()
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.contentPartnerName", equalTo("Related Content Partner")))
            .andExpect(jsonPath("$.contractDocument", equalTo("http://server.com/oranges.png")))
            .andExpect(jsonPath("$.contractDates.start", equalTo("2010-12-31")))
            .andExpect(jsonPath("$.contractDates.end", equalTo("2011-01-31")))
            .andExpect(jsonPath("$.daysBeforeTerminationWarning", equalTo(30)))
            .andExpect(jsonPath("$.yearsForMaximumLicense", equalTo(4)))
            .andExpect(jsonPath("$.daysForSellOffPeriod", equalTo(12)))
            .andExpect(jsonPath("$.royaltySplit.download", closeTo(19.333333, 0.0001)))
            .andExpect(jsonPath("$.royaltySplit.streaming", closeTo(50.0, 0.0001)))
            .andExpect(jsonPath("$.minimumPriceDescription", equalTo("Minimum prices are cool")))
            .andExpect(jsonPath("$.remittanceCurrency", equalTo("GBP")))
            .andExpect(jsonPath("$.restrictions").exists())
    }

    @Test
    fun `creates contract with only required values and fetches it`() {
        val contractUrl = mockMvc.perform(
            post("/v1/content-partner-contracts").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "contentPartnerName": "Related Content Partner"
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(
                get(contractUrl).asBoclipsEmployee()
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.contentPartnerName", equalTo("Related Content Partner")))
    }
}