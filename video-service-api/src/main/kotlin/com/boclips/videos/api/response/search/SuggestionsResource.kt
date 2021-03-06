package com.boclips.videos.api.response.search

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

data class SuggestionsResource(
    val suggestionTerm: String,
    val channels: List<ChannelSuggestionResource>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subjects: List<SubjectSuggestionResource>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val _links: Map<String, HateoasLink>?
)

data class ChannelSuggestionResource(
    val id: String,
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)

data class SubjectSuggestionResource(
    val id: String,
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>?
)
