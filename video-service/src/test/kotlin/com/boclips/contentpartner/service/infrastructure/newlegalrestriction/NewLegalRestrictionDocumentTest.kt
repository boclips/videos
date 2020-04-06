package com.boclips.contentpartner.service.infrastructure.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.NewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class NewLegalRestrictionDocumentTest {
    @Test
    fun toNewLegalRestriction() {

        val document =
            NewLegalRestrictionDocument(
                id = "contentPartner",
                restrictions = listOf(SingleLegalRestriction(id = "idOne", text = "restriction one text"))
            )

        val restrictions = document.toNewLegalRestriction()

        Assertions.assertThat(restrictions).isEqualTo(
            NewLegalRestriction(
                id = "contentPartner",
                restrictions = listOf(SingleLegalRestriction(id = "idOne", text = "restriction one text"))
            )
        )
    }

    @Test
    fun toNewLegalRestrictionDocument() {

        val restrictions = NewLegalRestriction(
            id = "contentPartner",
            restrictions = listOf(SingleLegalRestriction(id = "idOne", text = "restriction one text"))
        )

        val document = NewLegalRestrictionDocument.toNewLegalRestrictionDocument(
            restrictions
        )

        Assertions.assertThat(document).isEqualTo(
            NewLegalRestrictionDocument(
                id = "contentPartner",
                restrictions = listOf(SingleLegalRestriction(id = "idOne", text = "restriction one text"))
            )
        )
    }
}