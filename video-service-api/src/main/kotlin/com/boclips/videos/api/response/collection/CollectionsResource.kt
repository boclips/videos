package com.boclips.videos.api.response.collection

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedResources

class CollectionsResource(
    var _embedded: CollectionsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedResources.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class CollectionsWrapperResource(
    val collections: List<CollectionResource>
)
