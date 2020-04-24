package com.boclips.videos.api.response.search

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

class SuggestionsResource(
    var _embedded: SuggestionsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)

data class SuggestionsWrapperResource(
    val suggestions: SuggestionDetailsResource
)

data class SuggestionDetailsResource(
    val contentPartners: List<ContentPartnerSuggestionResource>
)

data class ContentPartnerSuggestionResource(
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)
