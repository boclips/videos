package com.boclips.videos.service.application.video.indexing

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import mu.KLogging

class VideoIndexUpdater(
    private val videoRepository: VideoRepository,
    private val contentPartnerService: ContentPartnerService,
    private val videoSearchService: VideoSearchService,
    private val legacyVideoSearchService: LegacyVideoSearchService
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun videoCreated(videoCreatedEvent: com.boclips.eventbus.events.video.VideoCreated) {
        val videoId = videoCreatedEvent.video.id.value
        val createdVideo = videoRepository.find(VideoId(value = videoId))
            ?: throw VideoNotFoundException(VideoId(value = videoId))

        updateIndexWith(createdVideo)
    }

    @BoclipsEventListener
    fun videoUpdated(videoUpdatedEvent: com.boclips.eventbus.events.video.VideoUpdated) {
        val videoId = videoUpdatedEvent.video.id.value
        val updatedVideo = videoRepository.find(VideoId(value = videoId))
            ?: throw VideoNotFoundException(VideoId(value = videoId))

        updateIndexWith(updatedVideo)
    }

    @BoclipsEventListener
    fun videosUpdated(videosUpdatedEvent: com.boclips.eventbus.events.video.VideosUpdated) {
        val videos = videosUpdatedEvent.videos.mapNotNull {
            videoRepository.find(VideoId(it.id.value)) ?: logger.info { "Video ${it.id.value} not found" }.let { null }
        }

        bulkUpdateStreamIndex(videos)
        bulkUpdateDownloadIndex(videos)
    }

    private fun updateIndexWith(updatedVideo: Video) {
        try {
            updateDownloadIndex(updatedVideo)
        } catch (e: Exception) {
            logger.info {
                "Failed to update video: ${updatedVideo.videoId} for ${DistributionMethod.DOWNLOAD}" +
                    "Exception: ${e.message}"
            }
        }

        updateStreamIndex(updatedVideo)
    }

    private fun bulkUpdateStreamIndex(updatedVideos: List<Video>) {
        val videosToUpsert = updatedVideos.filter { isStreamable(it) }
        val videosToRemove = updatedVideos.filterNot { isStreamable(it) }

        videoSearchService.upsert(videosToUpsert.asSequence())
        logger.info { "Indexed ${videosToUpsert.size} videos for ${DistributionMethod.STREAM}" }

        videoSearchService.bulkRemoveFromSearch(videosToRemove.map { it.videoId.value })
        logger.info { "Removed ${videosToRemove.size} videos for ${DistributionMethod.STREAM}" }
    }

    private fun bulkUpdateDownloadIndex(updatedVideos: List<Video>) {
        val videosToUpsert = updatedVideos
            .filter { isDownloadable(it) }
            .filter { it.isBoclipsHosted() }

        val videosToRemove = updatedVideos.filterNot { isDownloadable(it) }

        legacyVideoSearchService.upsert(videosToUpsert.map(VideoToLegacyVideoMetadataConverter::convert).asSequence())
        logger.info { "Indexed ${videosToUpsert.size} videos for ${DistributionMethod.DOWNLOAD}" }

        legacyVideoSearchService.bulkRemoveFromSearch(videosToRemove.map { it.videoId.value })
        logger.info { "Removed ${videosToRemove.size} videos for ${DistributionMethod.DOWNLOAD}" }
    }

    private fun updateStreamIndex(updatedVideo: Video) {
        if (isStreamable(updatedVideo)) {
            videoSearchService.upsert(sequenceOf(updatedVideo))
            logger.info { "Indexed video ${updatedVideo.videoId} for ${DistributionMethod.STREAM}" }
        } else {
            videoSearchService.removeFromSearch(updatedVideo.videoId.value)
            logger.info { "Removed video ${updatedVideo.videoId} from ${DistributionMethod.STREAM}" }
        }
    }

    private fun updateDownloadIndex(updatedVideo: Video) {
        if (isDownloadable(updatedVideo)) {
            if (updatedVideo.isBoclipsHosted()) {
                legacyVideoSearchService.upsert(sequenceOf(VideoToLegacyVideoMetadataConverter.convert(updatedVideo)))
                logger.info { "Indexed video ${updatedVideo.videoId} for ${DistributionMethod.DOWNLOAD}" }
            }
        } else {
            legacyVideoSearchService.removeFromSearch(updatedVideo.videoId.value)
            logger.info { "Removed video ${updatedVideo.videoId} from ${DistributionMethod.DOWNLOAD}" }
        }
    }

    private fun isDownloadable(video: Video): Boolean {
        return contentPartnerService.findAvailabilityFor(video.contentPartner.contentPartnerId).isDownloadable()
    }

    private fun isStreamable(video: Video): Boolean {
        return contentPartnerService.findAvailabilityFor(video.contentPartner.contentPartnerId).isStreamable()
    }
}
