package com.boclips.contentpartner.service.domain.model.newlegalrestriction

interface NewLegalRestrictionsRepository {
    fun findAll(): List<NewLegalRestriction>
    fun create(id: String, restrictions: List<SingleLegalRestriction>): NewLegalRestriction
    fun findOne(type: String): NewLegalRestriction?
}