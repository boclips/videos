package com.boclips.videos.service.domain.model.legal.restrictions

interface LegalRestrictionsRepository {

    fun create(text: String): LegalRestrictions

    fun findAll(): List<LegalRestrictions>
}