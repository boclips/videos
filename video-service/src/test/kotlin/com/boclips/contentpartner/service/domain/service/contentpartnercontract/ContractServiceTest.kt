package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.CreateContractResult
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.UpdateContractResult
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
            saveContentPartnerContract(name = "i exist")

            val result = contractService.create(ContentPartnerContractFactory.sample(contentPartnerName = "i exist"))
                as CreateContractResult.NameConflict

            assertThat(result.name).isEqualTo("i exist")
        }
    }

    @Nested
    inner class Updating() {
        @Test
        fun `can update a contract`() {
            val contract = saveContentPartnerContract(name = "old")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = contract.id, commands = listOf(
                        ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                            contractContentPartnerId = contract.id,
                            contentPartnerName = "new"
                        )
                    )
                )
            ) as UpdateContractResult.Success

            assertThat(result.contract).isEqualTo(contract.copy(contentPartnerName = "new"))
        }

        @Test
        fun `returns error when updating to a duplicate name`() {
            val contract = saveContentPartnerContract(name = "old")
            saveContentPartnerContract("i exist")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = contract.id, commands = listOf(
                        ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                            contractContentPartnerId = contract.id,
                            contentPartnerName = "i exist"
                        )
                    )
                )
            ) as UpdateContractResult.NameConflict

            assertThat(result.name).isEqualTo("i exist")
        }

        @Test
        fun `returns error when updating a non-existent contract`() {
            val missingId = ContentPartnerContractId("missing")

            val result = contractService.update(
                SingleContractUpdate(
                    contractId = missingId, commands = listOf(
                        ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                            contractContentPartnerId = missingId,
                            contentPartnerName = "i exist"
                        )
                    )
                )
            ) as UpdateContractResult.MissingContract

            assertThat(result.contractId).isEqualTo(missingId)
        }
    }
}
