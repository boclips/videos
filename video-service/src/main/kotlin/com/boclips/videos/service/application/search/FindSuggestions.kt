package com.boclips.videos.service.application.search

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.Suggestions
import mu.KLogging

class FindSuggestions(private val contentPartnerRepository: ContentPartnerRepository) {
    companion object : KLogging()

    operator fun invoke(query: String): Suggestions {
        val contentPartners = contentPartnerRepository.findByName(query)
            .take(10)
            .map { it.name }

        return Suggestions(contentPartners = contentPartners)
    }
}
