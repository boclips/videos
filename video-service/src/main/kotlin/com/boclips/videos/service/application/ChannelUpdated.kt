package com.boclips.videos.service.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class ChannelUpdated(private val videoRepository: VideoRepository) {
    companion object : KLogging()

    @BoclipsEventListener
    fun contentPartnerUpdated(contentPartnerUpdatedEvent: ContentPartnerUpdated) {
        val contentPartner = contentPartnerUpdatedEvent.contentPartner
        logger.info { "Starting updating videos for content partner: $contentPartner" }

        val channelId =
            ChannelId(value = contentPartner.id.value)

        videoRepository.streamUpdate(VideoFilter.ChannelIdIs(channelId = channelId)) { videos ->
            videos.flatMap { video ->
                val updateContentPartner = VideoUpdateCommand.ReplaceChannel(
                    videoId = video.videoId,
                    channel = Channel(
                        channelId = channelId,
                        name = contentPartner.name
                    )
                )
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
                val updateLegalRestrictions = contentPartner.legalRestrictions?.let {
                    VideoUpdateCommand.ReplaceLegalRestrictions(
                        videoId = video.videoId,
                        text = contentPartner.legalRestrictions
                    )
                }

                val updateLanguage = contentPartner.details?.language?.let {
                    VideoUpdateCommand.ReplaceLanguage(
                        videoId = video.videoId,
                        language = it
                    )
                }

                listOfNotNull(
                    updateContentPartner,
                    updateAgeRanges,
                    updateLegalRestrictions,
                    updateLanguage
                )
            }
        }

        logger.info { "Finished updating videos for content partner: $contentPartner" }
    }
}
