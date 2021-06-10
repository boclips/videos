package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

class ChannelFactory {
    companion object {
        fun create(id: String, name: String): Channel {
            return Channel(
                id = ChannelId(id),
                name = name,
                distributionMethods = emptySet(),
                ingest = ManualIngest,
                legalRestriction = null,
                remittance = null,
                description = null,
                contentCategories = null,
                language = null,
                notes = null,
                contentTypes = null,
                pedagogyInformation = null,
                marketingInformation = null,
                contract = null,
                taxonomy = null,
                visibility = null
            )
        }

        fun createSuggestion(id: String, name: String): ChannelSuggestion {
            return ChannelSuggestion(
                id = ChannelId(id),
                name = name
            )
        }
    }
}
