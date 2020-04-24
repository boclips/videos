package com.boclips.videos.service.application.search

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.search.Suggestions
import mu.KLogging

class FindSuggestions(private val contentPartnerRepository: ContentPartnerRepository) {
    companion object : KLogging()

    operator fun invoke(query: String): Suggestions {
        return Suggestions(contentPartners = contentPartnerRepository.findAllByNameMatch(query).map { it.name })
    }
}
