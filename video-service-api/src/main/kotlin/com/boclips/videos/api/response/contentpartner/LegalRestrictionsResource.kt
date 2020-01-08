package com.boclips.videos.api.response.contentpartner

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "legalRestrictions")
data class LegalRestrictionsResource(val id: String, val text: String)