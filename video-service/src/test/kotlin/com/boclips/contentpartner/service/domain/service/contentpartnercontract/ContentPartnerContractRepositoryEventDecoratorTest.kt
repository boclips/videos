package com.boclips.contentpartner.service.domain.service.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contract.ContractUpdated
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentPartnerContractRepositoryEventDecoratorTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contractRepository: ContentPartnerContractRepositoryEventDecorator

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
        val contract = saveContentPartnerContract(name = "old contract")
        fakeEventBus.clearState()

        contractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                    contentPartnerName = "new name",
                    contractContentPartnerId = contract.id
                )
            )
        )

        val event = fakeEventBus.getEventOfType(ContractUpdated::class.java)

        assertThat(event.contract.name).isEqualTo("new name")
        assertThat(event.contract.contractId.value).isEqualTo(contract.id.value)
    }
}
