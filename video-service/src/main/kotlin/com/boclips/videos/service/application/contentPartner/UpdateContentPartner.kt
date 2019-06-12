package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest

class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository
) {
    operator fun invoke(contentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        contentPartnerRepository.update(ContentPartnerUpdatesConverter().convert(id, request))

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: ${id.value}")

        updateContentPartnerInVideos(contentPartner)

        return contentPartner
    }

    private fun updateContentPartnerInVideos(
        contentPartner: ContentPartner
    ) {
        val videosAffected = videoRepository.findByContentPartnerName(contentPartnerName = contentPartner.name)

        val commands = videosAffected.flatMap { video ->
            listOf(
                VideoUpdateCommand.ReplaceContentPartner(videoId = video.videoId, contentPartner = contentPartner),
                VideoUpdateCommand.ReplaceAgeRange(videoId = video.videoId, ageRange = contentPartner.ageRange)
            )
        }

        videoRepository.bulkUpdate(commands)
    }
}