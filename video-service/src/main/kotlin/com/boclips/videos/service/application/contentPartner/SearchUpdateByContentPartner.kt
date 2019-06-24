package com.boclips.videos.service.application.contentPartner

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.ContentPartnerExclusionFromSearchRequested
import com.boclips.events.types.ContentPartnerInclusionInSearchRequested
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class SearchUpdateByContentPartner(
    val contentPartnerRepository: ContentPartnerRepository,
    val videoRepository: VideoRepository,
    val bulkUpdateVideo: BulkUpdateVideo
) {

    companion object : KLogging();

    @StreamListener(Subscriptions.CONTENT_PARTNER_EXCLUSION_FROM_SEARCH_REQUESTED)
    operator fun invoke(contentPartnerExclusionFromSearchEvent: ContentPartnerExclusionFromSearchRequested) {
        val contentPartnerId = ContentPartnerId(value = contentPartnerExclusionFromSearchEvent.contentPartnerId)
        logger.info { "Excluding content partner videos for ${contentPartnerId.value}" }

        try {
            updateSearchabilityOfContentPartner(contentPartnerId, false)

            val videos = videoRepository.findByContentPartnerId(contentPartnerId)
            updateVideosContentPartner(videos, contentPartnerId)

            bulkUpdateVideo.invoke(
                BulkUpdateRequest(
                    ids = videos.map { it.videoId.value },
                    status = VideoResourceStatus.SEARCH_DISABLED
                )
            )

            logger.info { "Finished excluding content partner videos for ${contentPartnerId.value}" }
        } catch (ex: Exception) {
            logger.info { "Exception whilst excluding content partner videos for ${contentPartnerId.value}: ${ex.message}" }
        }
    }

    @StreamListener(Subscriptions.CONTENT_PARTNER_INCLUSION_IN_SEARCH_REQUESTED)
    operator fun invoke(contentPartnerInclusionInSearchEvent: ContentPartnerInclusionInSearchRequested) {
        val contentPartnerId = ContentPartnerId(value = contentPartnerInclusionInSearchEvent.contentPartnerId)
        logger.info { "Including content partner videos for ${contentPartnerId.value}" }

        try {
            updateSearchabilityOfContentPartner(contentPartnerId, true)

            val videos = videoRepository.findByContentPartnerId(contentPartnerId)
            updateVideosContentPartner(videos, contentPartnerId)

            bulkUpdateVideo.invoke(
                BulkUpdateRequest(
                    ids = videos.map { it.videoId.value },
                    status = VideoResourceStatus.SEARCHABLE
                )
            )

            logger.info { "Finished including content partner videos for ${contentPartnerId.value}" }
        } catch (ex: Exception) {
            logger.info { "Exception whilst including content partner videos for ${contentPartnerId.value}: ${ex.message}" }
        }
    }

    private fun updateSearchabilityOfContentPartner(contentPartnerId: ContentPartnerId, searchable: Boolean) {
        contentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.SetSearchability(
                    contentPartnerId = contentPartnerId,
                    searchable = searchable
                )
            )
        )
    }

    private fun updateVideosContentPartner(videos: List<Video>, contentPartnerId: ContentPartnerId) {
        val contentPartner = contentPartnerRepository.findById(contentPartnerId)!!

        videoRepository.bulkUpdate(videos.map {
            VideoUpdateCommand.ReplaceContentPartner(
                it.videoId,
                contentPartner = contentPartner
            )
        })
    }
}