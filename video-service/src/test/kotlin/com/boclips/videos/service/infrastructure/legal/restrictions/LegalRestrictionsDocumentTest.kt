package com.boclips.videos.service.infrastructure.legal.restrictions

import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictionsId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class LegalRestrictionsDocumentTest {

    @Test
    fun toRestrictions() {
        val document = LegalRestrictionsDocument(
            id = ObjectId("5d81099a195d1081a0cfc4ea"),
            text = "Do not use this video ever"
        )

        val restrictions = document.toRestrictions()

        assertThat(restrictions).isEqualTo(
            LegalRestrictions(
                id = LegalRestrictionsId("5d81099a195d1081a0cfc4ea"),
                text = "Do not use this video ever"
            )
        )
    }

    @Test
    fun from() {
        val id = TestFactories.aValidId()
        val restrictions = LegalRestrictions(
            id = LegalRestrictionsId(id),
            text = "No restrictions"
        )

        val document = LegalRestrictionsDocument.from(restrictions)
        
        assertThat(document).isEqualTo(LegalRestrictionsDocument(
            id = ObjectId(id),
            text = "No restrictions"
        ))
    }
}