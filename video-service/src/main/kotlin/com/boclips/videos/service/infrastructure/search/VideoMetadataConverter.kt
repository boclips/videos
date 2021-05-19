package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
import com.boclips.search.service.domain.videos.model.VideoCategoryCodes
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.OrganisationsPrices
import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.channel.Availability
import com.boclips.videos.service.domain.model.video.prices.VideoWithPrices
import com.boclips.videos.service.domain.service.video.ContentEnrichers
import java.math.BigDecimal

object VideoMetadataConverter {
    fun convert(video: BaseVideo, videoAvailability: Availability): VideoMetadata {
        val subjects = video.subjects.items
            .map { SubjectMetadata(id = it.id.value, name = it.name) }
            .toSet()

        return VideoMetadata(
            id = video.videoId.value,
            title = video.title,
            rawTitle = video.title,
            description = video.description,
            contentProvider = video.channel.name,
            contentPartnerId = video.channel.channelId.value,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.playback.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playback.id.type),
            transcript = video.voice.transcript,
            isVoiced = video.isVoiced(),
            language = video.voice.language,
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max(),
            types = video.types.map { VideoTypeConverter.convert(it) },
            subjects = SubjectsMetadata(
                items = subjects,
                setManually = video.subjects.setManually
            ),
            promoted = video.promoted,
            meanRating = video.getRatingAverage(),
            eligibleForStream = videoAvailability.isStreamable(),
            eligibleForDownload = videoAvailability.isDownloadable(),
            attachmentTypes = attachmentTypes(video.attachments),
            deactivated = video.deactivated,
            ingestedAt = video.ingestedAt,
            prices = if (video is VideoWithPrices) convertPrices(video.prices) else null,
            categoryCodes = VideoCategoryCodes(convertCategories(video.categories))
        )
    }

    private fun convertCategories(categories: Map<CategorySource, Set<CategoryWithAncestors>>): List<String> {
        return categories.flatMap { source ->
            return@flatMap source.value.flatMap { category ->
                listOf(category.codeValue.value) + category.ancestors.map { it.value }
            }
        }.sorted()
    }

    private fun convertPrices(prices: OrganisationsPrices): Map<String, BigDecimal> {
        val default = "DEFAULT" to prices.default.amount
        return mapOf(default) + (prices.prices?.map { it.key.value to it.value.amount }?.toMap() ?: emptyMap())
    }

    private fun tagsFrom(video: BaseVideo): List<String> {
        val tags = video.tags.map { it.tag.label }.toMutableList()

        if (ContentEnrichers.isNews(video)) {
            tags.add("news")
        }

        return tags.toList()
    }

    private fun attachmentTypes(attachments: List<Attachment>): Set<String> =
        attachments.mapTo(HashSet()) { it.type.label }

    private fun convertPlaybackTypeToSourceType(playbackType: PlaybackProviderType): SourceType =
        when (playbackType) {
            KALTURA -> SourceType.BOCLIPS
            YOUTUBE -> SourceType.YOUTUBE
        }
}
