package com.boclips.videos.service.config.application

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.collection.AddVideoToCollection
import com.boclips.videos.service.application.collection.GetCollection
import com.boclips.videos.service.application.collection.GetDefaultCollection
import com.boclips.videos.service.application.collection.RemoveVideoFromCollection
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.application.video.DeleteVideos
import com.boclips.videos.service.application.video.PatchVideo
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.application.video.RefreshVideoDurations
import com.boclips.videos.service.application.video.search.GetAllVideosById
import com.boclips.videos.service.application.video.search.GetVideoById
import com.boclips.videos.service.application.video.search.GetVideosByQuery
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    val videoService: VideoService,
    val videoAssetRepository: VideoAssetRepository,
    val searchService: SearchService,
    val playbackRepository: PlaybackRepository,
    val legacySearchService: LegacySearchService,
    val collectionService: CollectionService,
    val eventService: EventService
) {

    @Bean
    fun searchVideo() = SearchVideo(
        getVideoById(),
        getAllVideosById(),
        getVideosByQuery(),
        videoAssetRepository
    )

    @Bean
    fun createVideo(
        searchVideo: SearchVideo,
        videoCounter: Counter
    ): CreateVideo {
        return CreateVideo(
            videoAssetRepository,
            searchVideo,
            CreateVideoRequestToAssetConverter(),
            searchService,
            playbackRepository,
            videoCounter,
            legacySearchService
        )
    }

    @Bean
    fun patchVideo(): PatchVideo {
        return PatchVideo(videoAssetRepository)
    }

    @Bean
    fun bulkUpdate(): BulkUpdateVideo {
        return BulkUpdateVideo(videoAssetRepository, searchService, legacySearchService)
    }

    @Bean
    fun deleteVideos(): DeleteVideos {
        return DeleteVideos(videoAssetRepository, searchService, playbackRepository)
    }

    @Bean
    fun getCollection(): GetCollection {
        return GetCollection(collectionService, VideoToResourceConverter())
    }

    @Bean
    fun getDefaultCollection(): GetDefaultCollection {
        return GetDefaultCollection(collectionService, VideoToResourceConverter())
    }

    @Bean
    fun addVideoToCollection(): AddVideoToCollection {
        return AddVideoToCollection(collectionService, eventService)
    }

    @Bean
    fun removeVideoFromCollection(): RemoveVideoFromCollection {
        return RemoveVideoFromCollection(collectionService, eventService)
    }

    @Bean
    fun rebuildSearchIndex(): RebuildSearchIndex {
        return RebuildSearchIndex(videoAssetRepository, searchService)
    }

    @Bean
    fun buildLegacySearchIndex(): BuildLegacySearchIndex {
        return BuildLegacySearchIndex(videoAssetRepository, legacySearchService)
    }

    @Bean
    fun refreshVideoDurations(): RefreshVideoDurations {
        return RefreshVideoDurations(videoAssetRepository, playbackRepository)
    }

    private fun getVideoById() =
        GetVideoById(
            videoService,
            videoToResourceConverter()
        )

    private fun getVideosByQuery() =
        GetVideosByQuery(
            videoService,
            videoToResourceConverter(),
            eventService
        )

    private fun getAllVideosById(): GetAllVideosById {
        return GetAllVideosById(
            videoService,
            videoToResourceConverter()
        )
    }

    private fun videoToResourceConverter(): VideoToResourceConverter {
        return VideoToResourceConverter()
    }
}