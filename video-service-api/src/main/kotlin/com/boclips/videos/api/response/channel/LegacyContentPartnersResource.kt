package com.boclips.videos.api.response.channel

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

open class LegacyContentPartnersResource(
    var _embedded: LegacyContentPartnerWrapperResource,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class LegacyContentPartnerWrapperResource(
    val contentPartners: List<ChannelResource> // Purposefully kept as contentPartners, need to check with external customers about renaming this
)
