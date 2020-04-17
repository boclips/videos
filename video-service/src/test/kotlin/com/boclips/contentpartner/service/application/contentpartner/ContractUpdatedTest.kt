package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContractUpdatedTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractRepository: ContentPartnerContractRepository

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Test
    fun `updating a contract also updates any associated content partners`() {
        val oldContract = saveContentPartnerContract(name = "old contract")
        val contentPartner = saveContentPartner(contractId = oldContract.id.value)
        val newContract = oldContract.copy(contentPartnerName = "new contract")

        contractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                    contractContentPartnerId = newContract.id,
                    contentPartnerName = newContract.contentPartnerName
                )
            )
        )

        val updateContentPartner = contentPartnerRepository.findById(contentPartner.contentPartnerId)!!

        assertThat(updateContentPartner.contract?.contentPartnerName).isEqualTo("new contract")
    }
}
