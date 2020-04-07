package com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestriction
import org.assertj.core.api.Assertions
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class ContractLegalRestrictionDocumentTest {
    @Test
    fun `converts document to domain object`() {
        val objId = ObjectId()

        val document =
            ContractLegalRestrictionDocument(
                id = objId,
                text = "text one"
            )

        val restrictions = document.toContractLegalRestriction()

        Assertions.assertThat(restrictions).isEqualTo(
            ContractLegalRestriction(
                id = objId.toHexString(),
                text = "text one"
            )
        )
    }

    @Test
    fun `converts domain object to document`() {
        val objId = ObjectId()

        val restrictions =
            ContractLegalRestriction(
                id = objId.toHexString(),
                text = "text one"
            )

        val document = ContractLegalRestrictionDocument.toContractLegalRestrictionDocument(
            restrictions
        )

        Assertions.assertThat(document).isEqualTo(
            ContractLegalRestrictionDocument(
                id = objId,
                text = "text one"
            )
        )
    }
}