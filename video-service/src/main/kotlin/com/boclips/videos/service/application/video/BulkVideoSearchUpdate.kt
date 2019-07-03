package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.VideosExclusionFromSearchRequested
import com.boclips.events.types.VideosInclusionInSearchRequested
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class BulkVideoSearchUpdate(
    val contentPartnerRepository: ContentPartnerRepository,
    val videoRepository: VideoRepository,
    private val bulkUpdateVideo: BulkUpdateVideo
) {

    companion object : KLogging();

    @StreamListener(Subscriptions.VIDEOS_EXCLUSION_FROM_SEARCH_REQUESTED)
    operator fun invoke(videoExclusionFromSearchEvent: VideosExclusionFromSearchRequested) {
        logger.info { "Video exclusion event received" }
        logger.info { "Excluding ${videoExclusionFromSearchEvent.videoIds.size} videos" }
        try {
            val videosIds = videoExclusionFromSearchEvent.videoIds
            bulkUpdateVideo.invoke(
                BulkUpdateRequest(
                    ids = videosIds,
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            logger.info { "Finished excluding ${videoExclusionFromSearchEvent.videoIds.size} videos" }
        } catch (ex: Exception) {
            logger.info { "Exception whilst excluding videos: ${ex.message}" }
        }
    }

    @StreamListener(Subscriptions.VIDEOS_INCLUSION_IN_SEARCH_REQUESTED)
    operator fun invoke(videosInclusionInSearchRequested: VideosInclusionInSearchRequested) {
        logger.info { "Video inclusion event received" }
        logger.info { "Including ${videosInclusionInSearchRequested.videoIds.size} videos" }
        try {
            val videosIds = videosInclusionInSearchRequested.videoIds
            bulkUpdateVideo.invoke(
                BulkUpdateRequest(
                    ids = videosIds,
                    status = VideoResourceStatus.SEARCHABLE
                )
            )

            logger.info { "Finished including ${videosInclusionInSearchRequested.videoIds.size} videos" }
        } catch (ex: Exception) {
            logger.info { "Exception whilst including videos: ${ex.message}" }
        }
    }
}