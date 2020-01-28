package com.boclips.videos.api.response.collection

import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "attachments")
data class AttachmentResource(
    @get:JsonView(PublicApiProjection::class)
    val id: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val type: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
