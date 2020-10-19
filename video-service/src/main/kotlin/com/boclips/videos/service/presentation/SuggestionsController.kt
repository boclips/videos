package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.search.SuggestionsResource
import com.boclips.videos.service.application.search.FindSuggestions
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.converters.SuggestionToResourceConverter
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class SuggestionsController(
    private val suggestionToResourceConverter: SuggestionToResourceConverter,
    private val findSuggestions: FindSuggestions,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    companion object : KLogging()

    @GetMapping("/suggestions")
    fun getNewSuggestions(
        @RequestParam(name = "query", required = true) query: String
    ): ResponseEntity<SuggestionsResource> {
        val suggestions = findSuggestions.byQuery(query = query, user = getCurrentUser())

        val resource = suggestionToResourceConverter.convertNewSuggestions(
            query = query,
            suggestions = suggestions
        )

        return ResponseEntity(
            resource, HttpStatus.OK
        )
    }
}
