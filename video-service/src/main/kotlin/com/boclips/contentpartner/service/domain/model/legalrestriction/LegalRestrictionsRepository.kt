package com.boclips.contentpartner.service.domain.model.legalrestriction

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestriction

    fun findById(id: LegalRestrictionsId): LegalRestriction?

    fun findAll(): List<LegalRestriction>
}
