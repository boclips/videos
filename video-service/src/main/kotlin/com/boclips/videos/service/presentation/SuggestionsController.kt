package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.search.SuggestionsRequest
import com.boclips.videos.api.response.search.ContentPartnerSuggestionResource
import com.boclips.videos.api.response.search.SuggestionDetailsResource
import com.boclips.videos.api.response.search.SuggestionsResource
import com.boclips.videos.api.response.search.SuggestionsWrapperResource
import com.boclips.videos.service.application.search.FindSuggestions
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1")
class SuggestionsController(
    private val findSuggestions: FindSuggestions,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    companion object : KLogging()

    @GetMapping("/suggestions")
    fun getSuggestions(@Valid request: SuggestionsRequest): ResponseEntity<SuggestionsResource> {
        val suggestions = findSuggestions(request.query)

        val suggestionsResource = SuggestionsResource(
            _embedded = SuggestionsWrapperResource(
                suggestions = SuggestionDetailsResource(
                    contentPartners = suggestions.contentPartners.map {
                        ContentPartnerSuggestionResource(
                            name = it,
                            _links = null
                        )
                    }
                )
            ),
            _links = null
        )

        return ResponseEntity(suggestionsResource, HttpStatus.OK)
    }
}
