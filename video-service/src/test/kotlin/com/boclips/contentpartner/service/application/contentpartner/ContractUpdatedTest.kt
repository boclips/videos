package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContractUpdatedTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractRepository: ContractRepository

    @Autowired
    lateinit var channelRepository: ChannelRepository

    @Test
    fun `updating a contract also updates any associated content partners`() {
        val oldContract = saveContract(name = "old contract")
        val contentPartner = saveChannel(contractId = oldContract.id.value)
        val newContract = oldContract.copy(contentPartnerName = "new contract")

        contractRepository.update(
            listOf(
                ContractUpdateCommand.ReplaceContentPartnerName(
                    contractId = newContract.id,
                    contentPartnerName = newContract.contentPartnerName
                )
            )
        )

        val updateContentPartner = channelRepository.findById(contentPartner.id)!!

        assertThat(updateContentPartner.contract?.contentPartnerName).isEqualTo("new contract")
    }
}
