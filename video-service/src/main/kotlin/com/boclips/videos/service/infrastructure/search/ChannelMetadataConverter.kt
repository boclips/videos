package com.boclips.videos.service.infrastructure.search

import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy.ChannelLevelTagging
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy.VideoLevelTagging
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.channels.model.IngestType
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.videos.api.common.IngestType.YOUTUBE
import com.boclips.contentpartner.service.domain.model.channel.ContentType as ChannelContentType

object ChannelMetadataConverter {
    fun convert(channel: Channel): ChannelMetadata {
        return ChannelMetadata(
            id = channel.id.value,
            name = channel.name,
            eligibleForStream = channel.isStreamable(),
            ingestType = when (channel.ingest) {
                CustomIngest -> IngestType.CUSTOM
                ManualIngest -> IngestType.MANUAL
                is MrssFeedIngest -> IngestType.MRSS
                is YoutubeScrapeIngest -> IngestType.YOUTUBE
            },
            contentTypes = channel.contentTypes?.map {
                when (it) {
                    ChannelContentType.NEWS -> ContentType.NEWS
                    ChannelContentType.STOCK -> ContentType.STOCK
                    ChannelContentType.INSTRUCTIONAL -> ContentType.INSTRUCTIONAL
                }
            } ?: emptyList(),
            taxonomy = Taxonomy(
                videoLevelTagging = channel.taxonomy is VideoLevelTagging,
                categories = convertCategories(channel)
            ),
            isYoutube = channel.ingest.type() == YOUTUBE
        )
    }

    private fun convertCategories(channel: Channel): Set<CategoryCode>? =
        (channel.taxonomy as? ChannelLevelTagging)?.categories?.map { CategoryCode(it.codeValue.value) }
            ?.toSet()
}
