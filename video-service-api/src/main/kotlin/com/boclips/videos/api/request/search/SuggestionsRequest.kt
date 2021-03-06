package com.boclips.videos.api.request.search

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SuggestionsRequest(
    @field:NotBlank(message = "Suggestion query must not be blank.")
    @field:Size(min = 3, max = 20, message = "Suggestion must contain between 3 and 20 characters.")
    val query: String?
)
