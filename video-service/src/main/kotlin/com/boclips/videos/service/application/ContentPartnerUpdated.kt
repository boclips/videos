package com.boclips.videos.service.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartner
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class ContentPartnerUpdated(private val videoRepository: VideoRepository) {
    companion object : KLogging()

    @BoclipsEventListener
    fun contentPartnerUpdated(contentPartnerUpdatedEvent: ContentPartnerUpdated) {
        val contentPartner = contentPartnerUpdatedEvent.contentPartner
        logger.info { "Starting updating videos for content partner: $contentPartner" }

        val contentPartnerId = ContentPartnerId(value = contentPartner.id.value)

        videoRepository.streamUpdate(VideoFilter.ContentPartnerIdIs(contentPartnerId = contentPartnerId)) { videos ->
            videos.flatMap { video ->
                val updateAgeRanges = video.ageRange.curatedManually.let { isCuratedManually ->
                    if (isCuratedManually) {
                        return@let null
                    }

                    VideoUpdateCommand.ReplaceAgeRange(
                        videoId = video.videoId,
                        ageRange = AgeRange.of(
                            min = contentPartner.pedagogy?.ageRange?.min,
                            max = contentPartner.pedagogy?.ageRange?.max,
                            curatedManually = false
                        )
                    )
                }

                listOfNotNull(
                    VideoUpdateCommand.ReplaceContentPartner(
                        videoId = video.videoId,
                        contentPartner = ContentPartner(
                            contentPartnerId = contentPartnerId,
                            name = contentPartner.name
                        )
                    ),
                    updateAgeRanges,
                    contentPartner. legalRestrictions?.let {
                        VideoUpdateCommand.ReplaceLegalRestrictions(
                            videoId = video.videoId,
                            text = contentPartner.legalRestrictions
                        )
                    }
                )
            }
        }

        logger.info { "Finished updating videos for content partner: $contentPartner" }
    }
}
