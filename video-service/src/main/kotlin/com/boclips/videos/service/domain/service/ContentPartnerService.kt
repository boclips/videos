package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.domain.model.video.contentpartner.Availability
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartner
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import org.springframework.stereotype.Component
import com.boclips.contentpartner.service.domain.model.channel.ChannelId as ContentPartnerServiceContentPartnerId

@Component
class ContentPartnerService(val channelRepository: ChannelRepository) {
    var idCache: Pair<ContentPartnerId, Availability>? = null

    fun findById(id: String): ContentPartner? {
        val contentPartner = find(
            ContentPartnerId(
                id
            )
        ) ?: throw ContentPartnerNotFoundException(id)

        return ContentPartner(
            contentPartnerId = ContentPartnerId(
                value = contentPartner.id.value
            ),
            name = contentPartner.name
        )
    }

    fun findAvailabilityFor(contentPartnerId: ContentPartnerId): Availability {
        idCache?.let {
            if (it.first == contentPartnerId) {
                return it.second
            }
        }

        return (channelRepository.findById(
            channelId = ContentPartnerServiceContentPartnerId(
                value = contentPartnerId.value
            )
        )?.let {
            when {
                it.isDownloadable() && it.isStreamable() -> Availability.ALL
                it.isDownloadable() -> Availability.DOWNLOAD
                it.isStreamable() -> Availability.STREAMING
                else -> Availability.NONE
            }
        } ?: Availability.NONE)
            .also {
                idCache = contentPartnerId to it
            }
    }

    private fun find(contentPartnerId: ContentPartnerId): com.boclips.contentpartner.service.domain.model.channel.Channel? {
        return channelRepository.findById(
            com.boclips.contentpartner.service.domain.model.channel.ChannelId(
                value = contentPartnerId.value
            )
        )
    }
}
