package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerContractNotFoundException
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.videos.api.request.contract.ContentPartnerContractRequest
import org.springframework.stereotype.Component

@Component
class UpdateContentPartnerContract
    (
    private val contractContentPartnerRepository: ContentPartnerContractRepository,
    private val contractContentPartnerUpdatesConverter: ContractContentPartnerConverter
) {
    operator fun invoke(contractId: String, updateRequest: ContentPartnerContractRequest): ContentPartnerContract {
        val id = ContentPartnerContractId(contractId)

        val updateCommands = contractContentPartnerUpdatesConverter.convert(id, updateRequest)
        contractContentPartnerRepository.update(updateCommands)

        return contractContentPartnerRepository.findById(id)
            ?: throw ContentPartnerContractNotFoundException("Could not find content partner contract : $contractId")
    }
}
