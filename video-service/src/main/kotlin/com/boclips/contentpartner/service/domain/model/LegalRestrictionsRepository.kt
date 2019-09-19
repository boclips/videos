package com.boclips.contentpartner.service.domain.model

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestrictions

    fun findById(id: LegalRestrictionsId): LegalRestrictions?

    fun findAll(): List<LegalRestrictions>
}
