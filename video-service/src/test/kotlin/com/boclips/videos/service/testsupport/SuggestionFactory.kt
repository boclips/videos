package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.Suggestions

class SuggestionFactory {
    companion object {
        fun create(contentPartners: List<String> = listOf("Ted", "Crash Course Biology")): Suggestions {
            return Suggestions(channels = contentPartners)
        }
    }
}
