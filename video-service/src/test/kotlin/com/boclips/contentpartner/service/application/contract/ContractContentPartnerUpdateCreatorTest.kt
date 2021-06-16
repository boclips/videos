package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContractIsRolling
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceCost
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceDaysBeforeTerminationWarning
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceDaysForSellOffPeriod
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceMinimumPriceDescription
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRemittanceCurrency
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRoyaltySplit
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceYearsForMaximumLicense
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.contract.ContractCostsRequest
import com.boclips.videos.api.request.contract.ContractRestrictionsRequest
import com.boclips.videos.api.request.contract.UpdateContractRequest
import com.boclips.videos.api.response.contract.ContractDatesResource
import com.boclips.videos.api.response.contract.ContractRoyaltySplitResource
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
    lateinit var contractConverter: ContractConverter

    lateinit var contract: Contract

    @BeforeEach
    fun setUp() {
        contract = createContract(
            ContentPartnerContractFactory.contentPartnerContractRequest()
        )
    }

    @Test
    fun `creates command for Contract Is Rolling`() {
        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(contentPartnerName = "name", contractIsRolling = true)
        )

        val command =
            commands.find { it is ReplaceContractIsRolling } as ReplaceContractIsRolling

        assertThat(command.contractIsRolling).isEqualTo(true)
    }

    @Test
    fun `creates command for ReplaceContractDates`() {
        val start = LocalDate.of(1990, 11, 18)
        val end = LocalDate.of(1990, 12, 18)

        val contractDates = ContractDatesResource(
            start = start.toString(),
            end = end.toString()
        )

        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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
        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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
        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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
        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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

        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
                contentPartnerName = "updated name",
                royaltySplit = ContractRoyaltySplitResource(
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
        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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

        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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
        val restrictions = ContractRestrictionsRequest(
            clientFacing = listOf("client facing restriction"),
            territory = "territory",
            licensing = "licensing",
            editing = "editing",
            marketing = "marketing",
            companies = "companies",
            payout = "payout",
            other = "other"
        )

        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(
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
        val costs = ContractCostsRequest(
            minimumGuarantee = listOf(BigDecimal.ONE),
            upfrontLicense = BigDecimal.TEN,
            technicalFee = BigDecimal.TEN,
            recoupable = false
        )

        val commands = contractConverter.convert(
            id = contract.id,
            updateContract = UpdateContractRequest(contentPartnerName = "updated name", costs = costs)
        )

        val command =
            commands.find { it is ReplaceCost } as ReplaceCost

        assertThat(command.costs.minimumGuarantee).contains(BigDecimal.ONE)
        assertThat(command.costs.upfrontLicense).isEqualTo(BigDecimal.TEN)
        assertThat(command.costs.technicalFee).isEqualTo(BigDecimal.TEN)
        assertThat(command.costs.recoupable).isEqualTo(false)
    }
}
