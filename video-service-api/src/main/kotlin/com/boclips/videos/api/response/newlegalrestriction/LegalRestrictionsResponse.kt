package com.boclips.videos.api.response.newlegalrestriction

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class NewLegalRestrictionsResource(
    var _embedded: List<LegalRestrictionsWrapperResource>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

class LegalRestrictionsWrapperResource(
    val id: String,
    val restrictions: List<SingleLegalRestrictionResponse>
)

class SingleLegalRestrictionResponse(
    val id: String,
    val text: String
)