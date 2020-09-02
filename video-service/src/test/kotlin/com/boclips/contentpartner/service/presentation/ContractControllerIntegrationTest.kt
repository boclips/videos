package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.net.URL

class ContractControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates contract with correct values and fetches it`() {
        val contractUrl = mockMvc.perform(
            post("/v1/contracts").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
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
                            },
                            "costs": {
                                "minimumGuarantee": [12.50],
                                "upfrontLicense": 3.1,
                                "recoupable": true,
                                "technicalFee": 11.30
                            }
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andReturn().response.getHeaders("Location").first()

        fakeSignedLinkProvider.setLink(URL("http://server.com/oranges.png#signed"))

        mockMvc.perform(
            get(contractUrl).asBoclipsEmployee()
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.contentPartnerName", equalTo("Related Content Partner")))
            .andExpect(jsonPath("$.contractDocument", equalTo("http://server.com/oranges.png#signed")))
            .andExpect(jsonPath("$.contractDates.start", equalTo("2010-12-31")))
            .andExpect(jsonPath("$.contractDates.end", equalTo("2011-01-31")))
            .andExpect(jsonPath("$.daysBeforeTerminationWarning", equalTo(30)))
            .andExpect(jsonPath("$.yearsForMaximumLicense", equalTo(4)))
            .andExpect(jsonPath("$.daysForSellOffPeriod", equalTo(12)))
            .andExpect(jsonPath("$.royaltySplit.download", closeTo(19.333333, 0.0001)))
            .andExpect(jsonPath("$.royaltySplit.streaming", closeTo(50.0, 0.0001)))
            .andExpect(jsonPath("$.minimumPriceDescription", equalTo("Minimum prices are cool")))
            .andExpect(jsonPath("$.remittanceCurrency", equalTo("GBP")))
            .andExpect(jsonPath("$.costs.minimumGuarantee[0]", closeTo(12.5, 0.0001)))
            .andExpect(jsonPath("$.costs.upfrontLicense", closeTo(3.1, 0.0001)))
            .andExpect(jsonPath("$.costs.recoupable", equalTo(true)))
            .andExpect(jsonPath("$.costs.technicalFee", closeTo(11.3, 0.0001)))
            .andExpect(jsonPath("$.restrictions").exists())
    }

    @Test
    fun `a 404 when fetching non-existent content partner`() {
        mockMvc.perform(get("/v1/contracts/missing").asBoclipsEmployee())
            .andExpect(status().isNotFound)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `a 403 when trying to create a contract with incorrect role`() {
        mockMvc.perform(post("/v1/contracts"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `a 409 when trying to create a contract with an pre-exisitng name`() {
        saveContract(name = "already here")

        mockMvc.perform(
            post("/v1/contracts").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "contentPartnerName": "already here"
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isConflict)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `creates contract with only required values and fetches it`() {
        val contractUrl = mockMvc.perform(
            post("/v1/contracts").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON)
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

    @Test
    fun `view all contracts`() {
        val firstContractId = saveContract(name = "the best videos")
        val secondContractId = saveContract(name = "okay videos")

        mockMvc.perform(get("/v1/contracts").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contracts", hasSize<Int>(2)))
            .andExpect(
                jsonPath(
                    "$._embedded.contracts[*].id",
                    contains(firstContractId.id.value, secondContractId.id.value)
                )
            )
    }

    @Test
    fun `view contracts with list projection`() {
        saveContract(name = "the best videos")

        mockMvc.perform(get("/v1/contracts?projection=list").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contracts", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.contracts[0].id", notNullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].contentPartnerName", notNullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].contractDocument", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].contractDates", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].contractIsRolling", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].daysBeforeTerminationWarning", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].yearsForMaximumLicense", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].daysForSellOffPeriod", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].royaltySplit", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].minimumPriceDescription", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].remittanceCurrency", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].restrictions", nullValue()))
            .andExpect(jsonPath("$._embedded.contracts[0].costs", nullValue()))

    }

    @Test
    fun `a 403 when viewing all contracts with incorrect role`() {
        mockMvc.perform(get("/v1/contracts"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `can page when fetching all contracts`() {
        saveContract(name = "the best videos")
        saveContract(name = "okay videos")

        mockMvc.perform(get("/v1/contracts?page=0&size=1").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contracts", hasSize<Int>(1)))
            .andExpect(jsonPath("$.page.size", equalTo(1)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
            .andExpect(jsonPath("$.page.totalElements", equalTo(2)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(2)))
    }

    @Test
    fun `retrieves a signed link`() {
        val sampleLink = URI("http://sample.com/").toURL()

        fakeSignedLinkProvider.setLink(sampleLink)

        val location = mockMvc.perform(
            post("/v1/contracts/signed-upload-link").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(
                    """{
                    |   "filename": "myImage.png"
                    |}
                """.trimMargin()
                )
        )
            .andExpect(status().isNoContent)
            .andExpect(MockMvcResultMatchers.header().exists("Location"))
            .andReturn().response.getHeaders("Location").first()

        Assertions.assertThat(sampleLink.toString()).isEqualTo(location)
    }

    @Test
    fun `updates the contract, and ensures contract document deletion works correctly`() {
        fakeSignedLinkProvider.setLink(URL("http://server.com/oranges.png#signed"))

        val contract = saveContract(name = "okay videos")

        val fullUpdate = """
            {
                "contentPartnerName": "Related Content Partner",
                "contractDocument": "http://server.com/oranges.png#signed",
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
                },
                "costs": {
                    "minimumGuarantee": [12.50],
                    "upfrontLicense": 3.1,
                    "recoupable": true,
                    "technicalFee": 11.30
                }
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(fullUpdate)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(jsonPath("$.contentPartnerName", equalTo("Related Content Partner")))
            .andExpect(jsonPath("$.contractDocument", equalTo("http://server.com/oranges.png#signed")))
            .andExpect(jsonPath("$.contractDates.start", equalTo("2010-12-31")))
            .andExpect(jsonPath("$.contractDates.end", equalTo("2011-01-31")))
            .andExpect(jsonPath("$.daysBeforeTerminationWarning", equalTo(30)))
            .andExpect(jsonPath("$.yearsForMaximumLicense", equalTo(4)))
            .andExpect(jsonPath("$.daysForSellOffPeriod", equalTo(12)))
            .andExpect(jsonPath("$.royaltySplit.download", closeTo(19.333333, 0.0001)))
            .andExpect(jsonPath("$.royaltySplit.streaming", closeTo(50.0, 0.0001)))
            .andExpect(jsonPath("$.minimumPriceDescription", equalTo("Minimum prices are cool")))
            .andExpect(jsonPath("$.remittanceCurrency", equalTo("GBP")))
            .andExpect(jsonPath("$.costs.minimumGuarantee[0]", closeTo(12.5, 0.0001)))
            .andExpect(jsonPath("$.costs.upfrontLicense", closeTo(3.1, 0.0001)))
            .andExpect(jsonPath("$.costs.recoupable", equalTo(true)))
            .andExpect(jsonPath("$.costs.technicalFee", closeTo(11.3, 0.0001)))
            .andExpect(jsonPath("$.restrictions").exists())
            .andExpect(jsonPath("$.costs").exists())

        val noDeletionOfContract = """
            {
                "contentPartnerName": "Updated Name"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(noDeletionOfContract)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(jsonPath("$.contractDocument", equalTo("http://server.com/oranges.png#signed")))

        val withDeletionOfContract = """
            {
                "contentPartnerName": "Related Content Partner",
                "contractDocument": null
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(withDeletionOfContract)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(jsonPath("$.contractDocument", equalTo(null)))
    }

    @Test
    fun `gets a 409 when updating the name to a pre-existing contract`() {
        saveContract(name = "already here")
        val contract = saveContract(name = "change me")

        val nameChange = """
            {
                "contentPartnerName": "already here"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/v1/contracts/${contract.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(nameChange)
        )
            .andExpect(status().isConflict)
    }
}
