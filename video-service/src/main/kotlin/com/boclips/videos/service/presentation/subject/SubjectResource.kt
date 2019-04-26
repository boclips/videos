package com.boclips.videos.service.presentation.subject

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "subjects")
class SubjectResource(val id: String, val name: String? = null)
