package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.Availability
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import org.springframework.stereotype.Component
import com.boclips.contentpartner.service.domain.model.ContentPartnerId as ContentPartnerServiceContentPartnerId

@Component
class ContentPartnerService(val contentPartnerRepository: ContentPartnerRepository) {
    fun findById(id: String): ContentPartner? {
        val contentPartner = find(ContentPartnerId(id)) ?: throw ContentPartnerNotFoundException(id)

        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = contentPartner.contentPartnerId.value),
            name = contentPartner.name
        )
    }

    fun findAvailabilityFor(contentPartnerId: ContentPartnerId): Availability {
        return contentPartnerRepository.findById(
            contentPartnerId = ContentPartnerServiceContentPartnerId(
                value = contentPartnerId.value
            )
        )?.let {
            when {
                it.isDownloadable() && it.isStreamable() -> Availability.ALL
                it.isDownloadable() -> Availability.DOWNLOAD
                it.isStreamable() -> Availability.STREAMING
                else -> Availability.NONE
            }
        } ?: Availability.NONE
    }

    private fun find(contentPartnerId: ContentPartnerId): com.boclips.contentpartner.service.domain.model.ContentPartner? {
        return contentPartnerRepository.findById(com.boclips.contentpartner.service.domain.model.ContentPartnerId(value = contentPartnerId.value))
    }
}
