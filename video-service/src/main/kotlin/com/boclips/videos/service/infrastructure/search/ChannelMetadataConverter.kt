package com.boclips.videos.service.infrastructure.search

import com.boclips.contentpartner.service.domain.model.channel.Taxonomy.ChannelLevelTagging
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy.VideoLevelTagging
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

object ChannelMetadataConverter {
    fun convert(channelSuggestion: ChannelSuggestion): ChannelMetadata {
        return ChannelMetadata(
            id = channelSuggestion.id.value,
            name = channelSuggestion.name,
            eligibleForStream = channelSuggestion.eligibleForStream,
            contentTypes = channelSuggestion.contentTypes.map {
                when (it) {
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.NEWS -> ContentType.NEWS
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.STOCK -> ContentType.STOCK
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.INSTRUCTIONAL -> ContentType.INSTRUCTIONAL
                }
            },
            taxonomy = Taxonomy(
                videoLevelTagging = channelSuggestion.taxonomy is VideoLevelTagging,
                categories = convertCategories(channelSuggestion)
            )
        )
    }

    private fun convertCategories(channelSuggestion: ChannelSuggestion): Set<CategoryCode>? =
        (channelSuggestion.taxonomy as? ChannelLevelTagging)?.categories?.map { CategoryCode(it.codeValue.value) }?.toSet()
}
