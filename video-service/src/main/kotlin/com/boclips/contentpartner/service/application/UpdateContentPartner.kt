package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.videos.service.domain.service.video.VideoService
import org.springframework.stereotype.Component

@Component
class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter,
    private val videoService: VideoService
) {
    operator fun invoke(contentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        val updateCommands = contentPartnerUpdatesConverter.convert(id, request)

        contentPartnerRepository.update(updateCommands)

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: ${id.value}")

        videoService.updateContentPartnerInVideos(contentPartner)

        return contentPartner
    }
}
