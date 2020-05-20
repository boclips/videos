package com.boclips.videos.api.response.channel

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class LegalRestrictionsResource(
    val _embedded: LegalRestrictionsWrapper,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>?
)

data class LegalRestrictionsWrapper(
    val legalRestrictions: List<LegalRestrictionResource>
)

