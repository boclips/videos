package com.boclips.contentpartner.service.domain.model.newlegalrestriction

data class NewLegalRestriction(val id: String, val restrictions: List<SingleLegalRestriction>)

data class SingleLegalRestriction(val id: String, val text: String)