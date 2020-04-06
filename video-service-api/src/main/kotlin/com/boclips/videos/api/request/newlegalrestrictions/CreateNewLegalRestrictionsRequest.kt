package com.boclips.videos.api.request.newlegalrestrictions

data class NewLegalRestrictionResource(
    val id: String,
    val restrictions: List<SingleLegalRestriction>
)

data class SingleLegalRestriction(
    val id: String,
    val text: String
)