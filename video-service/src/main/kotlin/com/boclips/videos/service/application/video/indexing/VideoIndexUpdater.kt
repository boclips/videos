package com.boclips.videos.service.application.video.indexing

import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.eventbus.BoclipsEventListener
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.video.PriceComputingService
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.prices.VideoWithPrices
import com.boclips.videos.service.domain.service.OrganisationService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.video.converters.VideoToLegacyVideoMetadataConverter
import mu.KLogging

class VideoIndexUpdater(
    private val videoRepository: VideoRepository,
    private val videoChannelService: VideoChannelService,
    private val videoIndex: VideoIndex,
    private val legacyVideoSearchService: LegacyVideoSearchService,
    private val organisationService: OrganisationService,
    private val priceComputingService: PriceComputingService
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun videoCreated(videoCreatedEvent: com.boclips.eventbus.events.video.VideoCreated) {
        val videoId = videoCreatedEvent.video.id.value
        val createdVideo = videoRepository.find(VideoId(value = videoId))
            ?: throw VideoNotFoundException(VideoId(value = videoId))

        logger.info { "Updating index for created video: ${videoCreatedEvent.video.id.value}" }

        updateIndexWith(createdVideo)
    }

    @BoclipsEventListener
    fun videoUpdated(videoUpdatedEvent: com.boclips.eventbus.events.video.VideoUpdated) {
        val videoId = videoUpdatedEvent.video.id.value
        val updatedVideo = videoRepository.find(VideoId(value = videoId))
            ?: throw VideoNotFoundException(VideoId(value = videoId))

        logger.info { "Updating index for updated video: ${videoUpdatedEvent.video.id.value}" }

        updateIndexWith(updatedVideo)
    }

    @BoclipsEventListener
    fun videosUpdated(videosUpdatedEvent: com.boclips.eventbus.events.video.VideosUpdated) {
        val videos = videosUpdatedEvent.videos.mapNotNull {
            videoRepository.find(VideoId(it.id.value)) ?: logger.info { "Video ${it.id.value} not found" }.let { null }
        }

        bulkUpdateIndex(videos)
        bulkUpdateLegacyIndex(videos)
    }

    private fun updateIndexWith(updatedVideo: Video) {
        try {
            updateLegacyIndex(updatedVideo)
        } catch (e: Exception) {
            logger.info {
                "Failed to update video: ${updatedVideo.videoId} for ${DistributionMethod.DOWNLOAD}" +
                    "Exception: ${e.message}"
            }
        }

        updateIndex(updatedVideo)
    }

    private fun bulkUpdateIndex(updatedVideos: List<Video>) {
        val organisationsWithPrices = organisationService.getOrganisationsWithCustomPrices()

        val hydratedVideos = updatedVideos.map { video ->
            priceComputingService.computeVideoOrganisationPrices(
                videoId = video.videoId,
                videoTypes = video.types,
                playback = video.playback,
                channel = video.channel.channelId,
                organisationsPrices = organisationsWithPrices
            )?.let { VideoWithPrices(video = video, prices = it) }
                ?: video
        }


        videoIndex.upsert(hydratedVideos.asSequence())
        logger.info { "Indexed ${updatedVideos.size} videos " }
    }

    private fun bulkUpdateLegacyIndex(updatedVideos: List<Video>) {
        val videosToUpsert = updatedVideos
            .filter { isDownloadable(it) }
            .filter { it.isBoclipsHosted() }

        val videosToRemove = updatedVideos.filterNot { isDownloadable(it) }

        legacyVideoSearchService.upsert(videosToUpsert.map(VideoToLegacyVideoMetadataConverter::convert).asSequence())
        logger.info { "Indexed ${videosToUpsert.size} videos for ${DistributionMethod.DOWNLOAD}" }

        legacyVideoSearchService.bulkRemoveFromSearch(videosToRemove.map { it.videoId.value })
        logger.info { "Removed ${videosToRemove.size} videos for ${DistributionMethod.DOWNLOAD}" }
    }

    private fun updateIndex(updatedVideo: Video) {
        val organisationsWithPrices = organisationService.getOrganisationsWithCustomPrices()

        val video = priceComputingService.computeVideoOrganisationPrices(
            videoId = updatedVideo.videoId,
            videoTypes = updatedVideo.types,
            playback = updatedVideo.playback,
            channel = updatedVideo.channel.channelId,
            organisationsPrices = organisationsWithPrices
        )?.let { VideoWithPrices(video = updatedVideo, prices = it) }
            ?: updatedVideo

        videoIndex.upsert(sequenceOf(video))
        logger.info { "Indexed video ${updatedVideo.videoId} " }
    }

    private fun updateLegacyIndex(updatedVideo: Video) {
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
        return videoChannelService.findAvailabilityFor(video.channel.channelId).isDownloadable()
    }
}
