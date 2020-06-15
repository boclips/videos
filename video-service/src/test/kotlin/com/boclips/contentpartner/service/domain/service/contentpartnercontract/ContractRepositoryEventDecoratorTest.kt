package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.service.contract.ContractRepositoryEventDecorator
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contract.ContractUpdated
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContractRepositoryEventDecoratorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractRepository: ContractRepositoryEventDecorator

    @Test
    fun `publishes a ContractUpdate event on create`() {
        val id = ObjectId().toHexString()

        contractRepository.create(
            ContentPartnerContractFactory.sample(
                id = id,
                contentPartnerName = "contract name"
            )
        )

        val event = fakeEventBus.getEventOfType(ContractUpdated::class.java)

        assertThat(event.contract.contractId.value).isEqualTo(id)
        assertThat(event.contract.name).isEqualTo("contract name")
    }

    @Test
    fun `publishes a ContractUpdate event on update`() {
        val contract = saveContract(name = "old contract")
        fakeEventBus.clearState()

        contractRepository.update(
            listOf(
                ContractUpdateCommand.ReplaceContentPartnerName(
                    contentPartnerName = "new name",
                    contractId = contract.id
                )
            )
        )

        val event = fakeEventBus.getEventOfType(ContractUpdated::class.java)

        assertThat(event.contract.name).isEqualTo("new name")
        assertThat(event.contract.contractId.value).isEqualTo(contract.id.value)
    }
}
