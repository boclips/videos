package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerContractDates
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRestrictions
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRoyaltySplit
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ContentPartnerContractToResourceConverterTest {
    lateinit var converter: ContentPartnerContractToResourceConverter

    @BeforeEach
    fun setUp() {
        converter = ContentPartnerContractToResourceConverter(mock())
    }

    @Test
    fun `can convert a contract to a resource`() {
        val contract = ContentPartnerContractFactory.sample(
            id = "123",
            contentPartnerName = "name",
            contractDocument = "https://iamthe.document",
            contractDates = ContentPartnerContractDates(
                start = LocalDate.of(2011, 1, 1),
                end = LocalDate.of(2022, 2, 2)
            ),
            daysBeforeTerminationWarning = 100,
            yearsForMaximumLicense = 10,
            daysForSellOffPeriod = 10,
            royaltySplit = ContentPartnerContractRoyaltySplit(
                streaming = 0.4f,
                download = 0.3f
            ),
            minimumPriceDescription = "a minimum price",
            remittanceCurrency = "GBP",
            restrictions = ContentPartnerContractRestrictions(
                clientFacing = listOf("Not suitable for the young"),
                territory = "Austraya mate",
                licensing = "Not in Canada",
                editing = "None",
                marketing = "Cannot use logo",
                companies = "Cobra",
                payout = "All the money goes to charity",
                other = "Cannot use at 11:45pm"
            )
        )

        val resource = converter.convert(contract)

        assertThat(resource.id).isEqualTo("123")
        assertThat(resource.contentPartnerName).isEqualTo("name")
        assertThat(resource.contractDocument).isEqualTo("https://iamthe.document")
        assertThat(resource.contractDates.start).isEqualTo("2011-01-01")
        assertThat(resource.contractDates.end).isEqualTo("2022-02-02")
        assertThat(resource.daysBeforeTerminationWarning).isEqualTo(100)
        assertThat(resource.yearsForMaximumLicense).isEqualTo(10)
        assertThat(resource.daysForSellOffPeriod).isEqualTo(10)
        assertThat(resource.royaltySplit.download).isEqualTo(0.3f)
        assertThat(resource.royaltySplit.streaming).isEqualTo(0.4f)
        assertThat(resource.minimumPriceDescription).isEqualTo("a minimum price")
        assertThat(resource.remittanceCurrency).isEqualTo("GBP")

        assertThat(resource.restrictions.territory).isEqualTo("Austraya mate")
        assertThat(resource.restrictions.licensing).isEqualTo("Not in Canada")
        assertThat(resource.restrictions.editing).isEqualTo("None")
        assertThat(resource.restrictions.marketing).isEqualTo("Cannot use logo")
        assertThat(resource.restrictions.companies).isEqualTo("Cobra")
        assertThat(resource.restrictions.payout).isEqualTo("All the money goes to charity")
        assertThat(resource.restrictions.other).isEqualTo("Cannot use at 11:45pm")
    }

    @Test
    fun `can convert a list of resources`() {
        val contracts = listOf(
            ContentPartnerContractFactory.sample(id = "id1"),
            ContentPartnerContractFactory.sample(id = "id2")
        )

        val resources = converter.convert(contracts)

        assertThat(resources._embedded.contracts.map { it.id }).containsExactlyInAnyOrder("id1", "id2")
    }
}
