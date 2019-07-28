package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import mu.KLogging

class VideoSearchUpdater(
    private val videoRepository: VideoRepository,
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

    private fun updateIndexWith(updatedVideo: Video) {
        if (updatedVideo.distributionMethods.contains(DistributionMethod.DOWNLOAD)) {
            if (updatedVideo.isBoclipsHosted()) {
                legacyVideoSearchService.upsert(sequenceOf(VideoToLegacyVideoMetadataConverter.convert(updatedVideo)))
                logger.info { "Indexed video ${updatedVideo.videoId} for ${DistributionMethod.DOWNLOAD}" }
            }
        } else {
            legacyVideoSearchService.removeFromSearch(updatedVideo.videoId.value)
            logger.info { "Removed video ${updatedVideo.videoId} from ${DistributionMethod.DOWNLOAD}" }
        }

        if (updatedVideo.distributionMethods.contains(DistributionMethod.STREAM)) {
            videoSearchService.upsert(sequenceOf(updatedVideo))
            logger.info { "Indexed video ${updatedVideo.videoId} for ${DistributionMethod.STREAM}" }
        } else {
            videoSearchService.removeFromSearch(updatedVideo.videoId.value)
            logger.info { "Removed video ${updatedVideo.videoId} from ${DistributionMethod.STREAM}" }
        }
    }
}