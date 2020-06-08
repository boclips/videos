package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contract.ContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractToResourceConverter
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class ContractToResourceConverterTest {
    lateinit var converter: ContractToResourceConverter

    @BeforeEach
    fun setUp() {
        converter =
            ContractToResourceConverter(
                mock()
            )
    }

    @Test
    fun `can convert a contract to a resource`() {
        val contract = ContentPartnerContractFactory.sample(
            id = "123",
            contentPartnerName = "name",
            contractDocument = "https://iamthe.document",
            contractDates = ContractDates(
                start = LocalDate.of(2011, 1, 1),
                end = LocalDate.of(2022, 2, 2)
            ),
            contractIsRolling = true,
            daysBeforeTerminationWarning = 100,
            yearsForMaximumLicense = 10,
            daysForSellOffPeriod = 10,
            royaltySplit = ContractRoyaltySplit(
                streaming = 0.4f,
                download = 0.3f
            ),
            minimumPriceDescription = "a minimum price",
            remittanceCurrency = "GBP",
            restrictions = ContractRestrictions(
                clientFacing = listOf("Not suitable for the young"),
                territory = "Austraya mate",
                licensing = "Not in Canada",
                editing = "None",
                marketing = "Cannot use logo",
                companies = "Cobra",
                payout = "All the money goes to charity",
                other = "Cannot use at 11:45pm"
            ),
            costs = ContractCosts(
                minimumGuarantee = listOf(BigDecimal.ONE),
                upfrontLicense = BigDecimal.TEN,
                technicalFee = BigDecimal.ZERO,
                recoupable = true
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

        assertThat(resource.costs.minimumGuarantee).isEqualTo(listOf(BigDecimal.ONE))
        assertThat(resource.costs.upfrontLicense).isEqualTo(BigDecimal.TEN)
        assertThat(resource.costs.technicalFee).isEqualTo(BigDecimal.ZERO)
        assertThat(resource.costs.recoupable).isEqualTo(true)
    }

    @Test
    fun `can convert a list of resources`() {
        val contracts = ResultsPage(
            elements = listOf(
                ContentPartnerContractFactory.sample(id = "id1"),
                ContentPartnerContractFactory.sample(id = "id2")
            ),
            pageInfo = PageInfo(
                hasMoreElements = true,
                totalElements = 11,
                pageRequest = PageRequest(size = 2, page = 0)
            )
        )

        val resources = converter.convert(contracts)

        assertThat(resources._embedded.contracts.map { it.id }).containsExactlyInAnyOrder("id1", "id2")
        assertThat(resources.page?.number).isEqualTo(0)
        assertThat(resources.page?.size).isEqualTo(2)
        assertThat(resources.page?.totalElements).isEqualTo(11)
        assertThat(resources.page?.totalPages).isEqualTo(6)
    }
}
