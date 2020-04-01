package com.boclips.contentpartner.service.infrastructure.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class LegalRestrictionDocumentTest {

    @Test
    fun toRestrictions() {
        val document =
            LegalRestrictionsDocument(
                id = ObjectId("5d81099a195d1081a0cfc4ea"),
                text = "Do not use this video ever"
            )

        val restrictions = document.toRestrictions()

        assertThat(restrictions).isEqualTo(
            LegalRestriction(
                id = LegalRestrictionsId(
                    "5d81099a195d1081a0cfc4ea"
                ),
                text = "Do not use this video ever"
            )
        )
    }

    @Test
    fun from() {
        val id = TestFactories.aValidId()
        val restrictions =
            LegalRestriction(
                id = LegalRestrictionsId(
                    id
                ),
                text = "No restrictions"
            )

        val document = LegalRestrictionsDocument.from(restrictions)

        assertThat(document).isEqualTo(
            LegalRestrictionsDocument(
                id = ObjectId(id),
                text = "No restrictions"
            )
        )
    }
}
