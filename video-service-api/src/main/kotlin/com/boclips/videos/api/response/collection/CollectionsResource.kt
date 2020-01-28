package com.boclips.videos.api.response.collection

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.PagedModel

class CollectionsResource(
    var _embedded: CollectionsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedModel.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)

data class CollectionsWrapperResource(
    val collections: List<CollectionResource>
)
