package com.boclips.videos.api.response.contentpartner

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class ContentPartnerStatusesResource(
    var _embedded: ContentPartnerStatusWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class ContentPartnerStatusWrapperResource(
    val statuses: List<String>
)