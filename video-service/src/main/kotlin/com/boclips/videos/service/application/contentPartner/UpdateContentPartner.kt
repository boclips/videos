package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest

class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoService: VideoService
) {
    operator fun invoke(contentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        val updateCommands = ContentPartnerUpdatesConverter().convert(id, request)

        contentPartnerRepository.update(updateCommands)

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: ${id.value}")

        videoService.updateContentPartnerInVideos(contentPartner)

        return contentPartner
    }
}
