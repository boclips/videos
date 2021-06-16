package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contract.CreateContractResult
import com.boclips.contentpartner.service.domain.model.contract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contract.UpdateContractResult
import com.boclips.contentpartner.service.domain.service.contract.ContractService
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContractServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractService: ContractService

    @Nested
    inner class Creating() {
        @Test
        fun `can create a contract`() {
            val contract = ContentPartnerContractFactory.sample()
            val result = contractService.create(contract) as CreateContractResult.Success

            assertThat(result.contract).isEqualTo(contract)
        }

        @Test
        fun `returns error when trying to create with a duplicate name`() {
            saveContract(name = "i exist")

            val result = contractService.create(ContentPartnerContractFactory.sample(contentPartnerName = "i exist"))
                as CreateContractResult.NameConflict

            assertThat(result.name).isEqualTo("i exist")
        }
    }

    @Nested
    inner class Updating() {
        @Test
        fun `can update a contract`() {
            val contract = saveContract(name = "old")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = contract.id,
                    commands = listOf(
                        ContractUpdateCommand.ReplaceContentPartnerName(
                            contractId = contract.id,
                            contentPartnerName = "new"
                        )
                    )
                )
            ) as UpdateContractResult.Success

            assertThat(result.contract).isEqualTo(contract.copy(contentPartnerName = "new"))
        }

        @Test
        fun `updating a content partner with the same name does not result in an error`() {
            val contract = saveContract(name = "new")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = contract.id,
                    commands = listOf(
                        ContractUpdateCommand.ReplaceContentPartnerName(
                            contractId = contract.id,
                            contentPartnerName = "new"
                        )
                    )
                )
            ) as UpdateContractResult.Success

            assertThat(result.contract).isEqualTo(contract)
        }

        @Test
        fun `returns error when updating to a duplicate name`() {
            val contract = saveContract(name = "old")
            saveContract("i exist")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = contract.id,
                    commands = listOf(
                        ContractUpdateCommand.ReplaceContentPartnerName(
                            contractId = contract.id,
                            contentPartnerName = "i exist"
                        )
                    )
                )
            ) as UpdateContractResult.NameConflict

            assertThat(result.name).isEqualTo("i exist")
        }

        @Test
        fun `returns error when updating a non-existent contract`() {
            val missingId = ContractId("missing")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = missingId,
                    commands = listOf(
                        ContractUpdateCommand.ReplaceContentPartnerName(
                            contractId = missingId,
                            contentPartnerName = "i exist"
                        )
                    )
                )
            ) as UpdateContractResult.MissingContract

            assertThat(result.contractId).isEqualTo(missingId)
        }
    }
}
