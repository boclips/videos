package com.boclips.contentpartner.service.domain.model.legalrestriction

import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestriction

    fun findById(id: LegalRestrictionsId): LegalRestriction?

    fun findAll(): List<LegalRestriction>
}
