package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.collection.CollectionId
import com.boclips.eventbus.domain.contentpartner.ChannelId
import com.boclips.eventbus.domain.user.UserId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.domain.video.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.eventbus.domain.video.Dimensions as EventDimensions
import com.boclips.eventbus.domain.video.VideoAsset as EventVideoAsset

class EventConverter {
    fun toVideoPayload(video: Video): com.boclips.eventbus.domain.video.Video {
        val originalDimensions = when (video.playback) {
            is VideoPlayback.StreamPlayback -> video.playback.originalDimensions?.let { toDimensionsPayload(it) }
            else -> null
        }

        val assets = when (video.playback) {
            is VideoPlayback.StreamPlayback -> video.playback.assets?.let { assets -> assets.map { toAssetPayload(it) } }
            else -> null
        }

        val subjects = toSubjectPayload(video.subjects.items)
        return com.boclips.eventbus.domain.video.Video.builder()
            .id(VideoId(video.videoId.value))
            .title(video.title)
            .channelId(ChannelId(video.contentPartner.contentPartnerId.value))
            .playbackProviderType(PlaybackProviderType.valueOf(video.playback.id.type.name))
            .subjects(subjects)
            .ageRange(toAgeRangePayload(video.ageRange))
            .durationSeconds(video.playback.duration.seconds.toInt())
            .type(toVideoType(video.type))
            .types(toVideoTypes(video.types))
            .ingestedAt(video.ingestedAt)
            .originalDimensions(originalDimensions)
            .assets(assets)
            .releasedOn(video.releasedOn)
            .build()
    }

    fun toAssetPayload(asset: VideoAsset): EventVideoAsset {
        return EventVideoAsset
            .builder()
            .bitrateKbps(asset.bitrateKbps)
            .id(asset.reference)
            .sizeKb(asset.sizeKb)
            .dimensions(toDimensionsPayload(asset.dimensions))
            .build()
    }

    fun toCollectionPayload(collection: Collection): com.boclips.eventbus.domain.collection.Collection {
        return com.boclips.eventbus.domain.collection.Collection.builder()
            .id(CollectionId(collection.id.value))
            .createdAt(collection.createdAt)
            .updatedAt(collection.updatedAt)
            .title(collection.title)
            .description(collection.description ?: "")
            .subjects(toSubjectPayload(collection.subjects))
            .videosIds(collection.videos.map { VideoId(it.value) })
            .ownerId(UserId(collection.owner.value))
            .isDiscoverable(collection.discoverable)
            .isDefault(collection.default)
            .ageRange(toAgeRangePayload(collection.ageRange))
            .bookmarks(collection.bookmarks.map { UserId(it.value) })
            .build()
    }

    private fun toDimensionsPayload(dimensions: Dimensions): EventDimensions {
        return EventDimensions(dimensions.width, dimensions.height)
    }

    private fun toVideoType(contentType: ContentType): VideoType {
        return when (contentType) {
            ContentType.INSTRUCTIONAL_CLIPS -> VideoType.INSTRUCTIONAL
            ContentType.NEWS -> VideoType.NEWS
            ContentType.STOCK -> VideoType.STOCK
        }
    }

    private fun toVideoTypes(contentTypes: List<ContentType>): List<VideoType> {
        return contentTypes.map { toVideoType(it) }
    }

    private fun toAgeRangePayload(ageRange: AgeRange): com.boclips.eventbus.domain.AgeRange {
        return com.boclips.eventbus.domain.AgeRange.builder()
            .min(ageRange.min())
            .max(ageRange.max())
            .build()
    }

    private fun toSubjectPayload(subjects: Set<Subject>): List<com.boclips.eventbus.domain.Subject> {
        return subjects.map {
            com.boclips.eventbus.domain.Subject.builder()
                .id(SubjectId(it.id.value))
                .name(it.name)
                .build()
        }
    }
}
