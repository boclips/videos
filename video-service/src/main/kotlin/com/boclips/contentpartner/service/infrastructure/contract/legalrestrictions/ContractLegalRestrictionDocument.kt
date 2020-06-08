package com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestriction
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ContractLegalRestrictionDocument(
    @BsonId
    val id: ObjectId,
    val text: String
) {
    fun toContractLegalRestriction(): ContractLegalRestriction {
        return ContractLegalRestriction(
            id = id.toHexString(),
            text = text
        )
    }

    companion object {
        fun toContractLegalRestrictionDocument(contractLegalRestriction: ContractLegalRestriction): ContractLegalRestrictionDocument {
            return ContractLegalRestrictionDocument(
                id = ObjectId(contractLegalRestriction.id),
                text = contractLegalRestriction.text
            )
        }
    }
}
