package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerContractNotFoundException
import com.boclips.contentpartner.service.application.exceptions.ContractConflictException
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.UpdateContractResult
import com.boclips.contentpartner.service.domain.service.contentpartnercontract.ContractService
import com.boclips.videos.api.request.contract.ContentPartnerContractRequest
import org.springframework.stereotype.Component

@Component
class UpdateContentPartnerContract
    (
    private val contractService: ContractService,
    private val contractContentPartnerUpdatesConverter: ContractContentPartnerConverter
) {
    operator fun invoke(contractId: String, updateRequest: ContentPartnerContractRequest): ContentPartnerContract {
        val id = ContentPartnerContractId(contractId)

        val updateCommands = contractContentPartnerUpdatesConverter.convert(id, updateRequest)

        val updateResult = contractService.update(SingleContractUpdate(id, updateCommands))

        return when (updateResult) {
            is UpdateContractResult.Success -> updateResult.contract
            is UpdateContractResult.NameConflict ->
                throw ContractConflictException(updateResult.name)
            is UpdateContractResult.MissingContract ->
                throw ContentPartnerContractNotFoundException("Could not find content partner contract : $contractId")
        }
    }
}
