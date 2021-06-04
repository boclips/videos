package com.boclips.videos.service.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import com.boclips.videos.service.domain.model.video.channel.ChannelId as VideoServiceChannelId

class ChannelUpdated(
    private val videoRepository: VideoRepository,
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun contentPartnerUpdated(contentPartnerUpdatedEvent: ContentPartnerUpdated) {
        val contentPartner = contentPartnerUpdatedEvent.contentPartner

        val videoServiceChannelId = VideoServiceChannelId(value = contentPartner.id.value)

        logger.info { "Updated channel index for channel: ${contentPartner.id}" }

        logger.info { "Starting updating videos for content partner: $contentPartner" }

        videoRepository.streamUpdate(VideoFilter.ChannelIdIs(channelId = videoServiceChannelId)) { videos ->
            videos.flatMap { video ->
                val updateContentPartner = VideoUpdateCommand.ReplaceChannel(
                    videoId = video.videoId,
                    channel = Channel(
                        channelId = videoServiceChannelId,
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

                val updateCategories = contentPartner.categories?.let { categoriesWithAncestors ->
                    VideoUpdateCommand.ReplaceCategories(
                        videoId = video.videoId,
                        source = CategorySource.CHANNEL,
                        categories = categoriesWithAncestors.map {
                            CategoryWithAncestors(
                                codeValue = CategoryCode(it.code),
                                description = it.description,
                                ancestors = it.ancestors.map { ancestor -> CategoryCode(ancestor) }.toSet()
                            )
                        }.toSet()
                    )
                }

                listOfNotNull(
                    updateContentPartner,
                    updateAgeRanges,
                    updateLegalRestrictions,
                    updateLanguage,
                    updateCategories
                )
            }
        }

        logger.info { "Finished updating videos for content partner: $contentPartner" }
    }
}
