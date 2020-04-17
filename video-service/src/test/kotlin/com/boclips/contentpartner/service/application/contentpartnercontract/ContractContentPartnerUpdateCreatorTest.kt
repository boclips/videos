package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContractIsRolling
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceCost
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceDaysBeforeTerminationWarning
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceDaysForSellOffPeriod
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceMinimumPriceDescription
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRemittanceCurrency
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRoyaltySplit
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceYearsForMaximumLicense
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.contract.ContentPartnerContractCostsRequest
import com.boclips.videos.api.request.contract.ContentPartnerContractRequest
import com.boclips.videos.api.request.contract.ContentPartnerContractRestrictionsRequest
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.threeten.bp.LocalDate
import java.math.BigDecimal
import java.util.Currency

class ContractContentPartnerUpdateCreatorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractContentPartnerConverter: ContractContentPartnerConverter

    lateinit var contract: ContentPartnerContract

    @BeforeEach
    fun setUp() {
        contract = createContentPartnerContract(
            ContentPartnerContractFactory.contentPartnerContractRequest()
        )
    }

    @Test
    fun `creates command for Contract Is Rolling`() {
        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(contentPartnerName = "name", contractIsRolling = true)
        )

        val command =
            commands.find { it is ReplaceContractIsRolling } as ReplaceContractIsRolling

        assertThat(command.contractIsRolling).isEqualTo(true)
    }

    @Test
    fun `creates command for ReplaceContractDates`() {
        val start = LocalDate.of(1990, 11, 18)
        val end = LocalDate.of(1990, 12, 18)

        val contractDates = ContentPartnerContractDatesResource(
            start = start.toString(),
            end = end.toString()
        )

        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                contractDates = contractDates
            )
        )

        val command =
            commands.find { it is ReplaceContractDates } as ReplaceContractDates

        assertThat(command.contractDates.start.toString()).isEqualTo(start.toString())
        assertThat(command.contractDates.end.toString()).isEqualTo(end.toString())
    }

    @Test
    fun `creates command for ReplaceDaysBeforeTerminationWarning`() {
        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                daysBeforeTerminationWarning = 4
            )
        )

        val command =
            commands.find { it is ReplaceDaysBeforeTerminationWarning } as ReplaceDaysBeforeTerminationWarning

        assertThat(command.daysBeforeTerminationWarning).isEqualTo(4)
    }

    @Test
    fun `creates command for ReplaceYearsForMaximumLicense`() {
        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                yearsForMaximumLicense = 7
            )
        )

        val command =
            commands.find { it is ReplaceYearsForMaximumLicense } as ReplaceYearsForMaximumLicense

        assertThat(command.yearsForMaximumLicense).isEqualTo(7)
    }

    @Test
    fun `creates command for ReplaceDaysForSellOffPeriod`() {
        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                daysForSellOffPeriod = 22
            )
        )

        val command =
            commands.find { it is ReplaceDaysForSellOffPeriod } as ReplaceDaysForSellOffPeriod

        assertThat(command.daysForSellOffPeriod).isEqualTo(22)
    }

    @Test
    fun `creates command for ReplaceRoyaltySplit`() {
        val download = 1.23.toFloat()
        val streaming = 22.2.toFloat()

        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                royaltySplit = ContentPartnerContractRoyaltySplitResource(
                    download = download,
                    streaming = streaming
                )
            )
        )

        val command =
            commands.find { it is ReplaceRoyaltySplit } as ReplaceRoyaltySplit

        assertThat(command.royaltySplit.streaming).isEqualTo(streaming)
        assertThat(command.royaltySplit.download).isEqualTo(download)
    }

    @Test
    fun `creates command for ReplaceMinimumPriceDescription`() {
        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                minimumPriceDescription = "minimum price"
            )
        )

        val command =
            commands.find { it is ReplaceMinimumPriceDescription } as ReplaceMinimumPriceDescription

        assertThat(command.minimumPriceDescription).isEqualTo("minimum price")
    }

    @Test
    fun `creates command for ReplaceRemittanceCurrency`() {
        val currency = Currency.getInstance("USD")

        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                remittanceCurrency = currency.toString()
            )
        )

        val command =
            commands.find { it is ReplaceRemittanceCurrency } as ReplaceRemittanceCurrency

        assertThat(command.remittanceCurrency).isEqualTo(currency)
    }

    @Test
    fun `creates command for ReplaceRestrictions`() {
        val restrictions = ContentPartnerContractRestrictionsRequest(
            clientFacing = listOf("client facing restriction"),
            territory = "territory",
            licensing = "licensing",
            editing = "editing",
            marketing = "marketing",
            companies = "companies",
            payout = "payout",
            other = "other"
        )

        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(
                contentPartnerName = "updated name",
                restrictions = restrictions
            )
        )

        val command =
            commands.find { it is ReplaceRestrictions } as ReplaceRestrictions

        assertThat(command.restrictions.clientFacing).contains("client facing restriction")
        assertThat(command.restrictions.territory).isEqualTo("territory")
        assertThat(command.restrictions.licensing).isEqualTo("licensing")
        assertThat(command.restrictions.editing).isEqualTo("editing")
        assertThat(command.restrictions.marketing).isEqualTo("marketing")
        assertThat(command.restrictions.companies).isEqualTo("companies")
        assertThat(command.restrictions.payout).isEqualTo("payout")
        assertThat(command.restrictions.other).isEqualTo("other")
    }

    @Test
    fun `creates command for ReplaceCost`() {
        val costs = ContentPartnerContractCostsRequest(
            minimumGuarantee = listOf(BigDecimal.ONE),
            upfrontLicense = BigDecimal.TEN,
            technicalFee = BigDecimal.TEN,
            recoupable = false
        )

        val commands = contractContentPartnerConverter.convert(
            id = contract.id,
            updateContract = ContentPartnerContractRequest(contentPartnerName = "updated name", costs = costs)
        )

        val command =
            commands.find { it is ReplaceCost } as ReplaceCost

        assertThat(command.costs.minimumGuarantee).contains(BigDecimal.ONE)
        assertThat(command.costs.upfrontLicense).isEqualTo(BigDecimal.TEN)
        assertThat(command.costs.technicalFee).isEqualTo(BigDecimal.TEN)
        assertThat(command.costs.recoupable).isEqualTo(false)
    }
}
