package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.collection.CollectionId
import com.boclips.eventbus.domain.contentpartner.ChannelId
import com.boclips.eventbus.domain.user.UserId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoId
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.eventbus.domain.category.CategoryWithAncestors as EventCategoryWithAncestors
import com.boclips.eventbus.domain.video.Dimensions as EventDimensions
import com.boclips.eventbus.domain.video.VideoAsset as EventVideoAsset
import com.boclips.eventbus.domain.video.VideoCategorySource as EventVideoCategorySource
import com.boclips.eventbus.domain.video.VideoTopic as EventVideoTopic
import com.boclips.eventbus.domain.video.VideoType as EventBusVideoType

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
            .description(video.description)
            .channelId(ChannelId(video.channel.channelId.value))
            .playbackProviderType(PlaybackProviderType.valueOf(video.playback.id.type.name))
            .playbackId(video.playback.id.value)
            .subjects(subjects)
            .ageRange(toAgeRangePayload(video.ageRange))
            .durationSeconds(video.playback.duration.seconds.toInt())
            .type(toVideoType(video.types.first()))
            .types(toVideoTypes(video.types))
            .ingestedAt(video.ingestedAt)
            .originalDimensions(originalDimensions)
            .assets(assets)
            .releasedOn(video.releasedOn)
            .promoted(video.promoted ?: false)
            .topics(toTopicsPayload(video.topics))
            .keywords(video.keywords)
            .sourceVideoReference(video.videoReference)
            .deactivated(video.deactivated)
            .hasTranscript(video.hasTranscript())
            .categories(toCategoriesPayload(video.categories))
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
            .promoted(collection.promoted)
            .build()
    }

    private fun toDimensionsPayload(dimensions: Dimensions): EventDimensions {
        return EventDimensions(dimensions.width, dimensions.height)
    }

    private fun toVideoType(videoType: VideoType): EventBusVideoType {
        return when (videoType) {
            VideoType.INSTRUCTIONAL_CLIPS -> EventBusVideoType.INSTRUCTIONAL
            VideoType.NEWS -> EventBusVideoType.NEWS
            VideoType.STOCK -> EventBusVideoType.STOCK
        }
    }

    private fun toVideoTypes(contentTypes: List<VideoType>): List<EventBusVideoType> {
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

    private fun toTopicsPayload(topics: Set<Topic>): List<EventVideoTopic> {
        return topics.map {
            toTopicPayload(it)
        }
    }

    private fun toTopicPayload(topic: Topic): EventVideoTopic {
        return EventVideoTopic.builder()
            .name(topic.name)
            .confidence(topic.confidence)
            .language(topic.language)
            .parent(topic.parent?.let { toTopicPayload(it) })
            .build()
    }

    private fun toCategoriesWithAncestorsPayload(categories: Set<CategoryWithAncestors>): MutableSet<EventCategoryWithAncestors> {
        return categories.map {
            EventCategoryWithAncestors.builder()
                .code(it.codeValue.value)
                .description(it.description)
                .ancestors(it.ancestors.map { it.value }.toSet())
                .build()
        }.toMutableSet()
    }

    private fun toVideoCategorySourcePayload(source: CategorySource): EventVideoCategorySource {
        return when (source) {
            CategorySource.CHANNEL -> EventVideoCategorySource.CHANNEL
            CategorySource.MANUAL -> EventVideoCategorySource.MANUAL
        }
    }

    private fun toCategoriesPayload(categories: Map<CategorySource, Set<CategoryWithAncestors>>): MutableMap<EventVideoCategorySource, MutableSet<EventCategoryWithAncestors>> {
        return categories.map {
            toVideoCategorySourcePayload(it.key) to toCategoriesWithAncestorsPayload(it.value)
        }.toMap(mutableMapOf())
    }
}
