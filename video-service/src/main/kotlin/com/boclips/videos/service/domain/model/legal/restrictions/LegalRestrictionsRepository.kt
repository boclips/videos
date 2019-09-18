package com.boclips.videos.service.domain.model.legal.restrictions

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestrictions

    fun findById(id: LegalRestrictionsId): LegalRestrictions?

    fun findAll(): List<LegalRestrictions>
}