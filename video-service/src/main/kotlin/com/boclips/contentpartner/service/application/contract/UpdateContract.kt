package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.application.exceptions.ContractConflictException
import com.boclips.contentpartner.service.application.exceptions.ContractNotFoundException
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.SingleContractUpdate
import com.boclips.contentpartner.service.domain.model.contract.UpdateContractResult
import com.boclips.contentpartner.service.domain.service.contract.ContractService
import com.boclips.videos.api.request.contract.UpdateContractRequest
import org.springframework.stereotype.Component

@Component
class UpdateContract(
    private val contractService: ContractService,
    private val contractUpdatesConverter: ContractConverter
) {
    operator fun invoke(contractId: String, updateRequest: UpdateContractRequest): Contract {
        val id = ContractId(contractId)

        val updateCommands = contractUpdatesConverter.convert(id, updateRequest)

        val updateResult = contractService.update(SingleContractUpdate(id, updateCommands))

        return when (updateResult) {
            is UpdateContractResult.Success -> updateResult.contract
            is UpdateContractResult.NameConflict ->
                throw ContractConflictException(updateResult.name)
            is UpdateContractResult.MissingContract ->
                throw ContractNotFoundException("Could not find content partner contract : $contractId")
        }
    }
}
