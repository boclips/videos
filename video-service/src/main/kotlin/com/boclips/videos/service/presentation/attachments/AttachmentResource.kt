package com.boclips.videos.service.presentation.attachments

import com.boclips.videos.service.presentation.projections.PublicApiProjection
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "attachments")
data class AttachmentResource(
    @get:JsonView(PublicApiProjection::class)
    val id: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val type: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val description: String? = null
)
