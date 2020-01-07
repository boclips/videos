package com.boclips.videos.api.response.contentpartner

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class ContentPartnersResource(
    var _embedded: ContentPartnerWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class ContentPartnerWrapperResource(
    val contentPartners: List<ContentPartnerResource>
)