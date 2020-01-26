package com.boclips.contentpartner.service.domain.model

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestriction

    fun findById(id: LegalRestrictionsId): LegalRestriction?

    fun findAll(): List<LegalRestriction>
}
