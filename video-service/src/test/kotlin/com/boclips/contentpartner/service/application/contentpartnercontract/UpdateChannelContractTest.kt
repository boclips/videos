package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.events.contract.ContractUpdated
import com.boclips.videos.api.request.contract.CreateContractRequest
import com.boclips.videos.api.request.contract.UpdateContractRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateChannelContractTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateContentPartnerContract: UpdateContentPartnerContract

    @Test
    fun `publishes a contract updated event`() {
        val contract = saveContentPartnerContract(name = "old contract name")

        fakeEventBus.clearState()

        updateContentPartnerContract(
            contractId = contract.id.value,
            updateRequest = UpdateContractRequest(contentPartnerName = "new contract name")
        )

        val event = fakeEventBus.getEventOfType(ContractUpdated::class.java)

        assertThat(event.contract.contractId.value).isEqualTo(contract.id.value)
        assertThat(event.contract.name).isEqualTo("new contract name")
    }
}
