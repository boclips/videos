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
    private val videoRepository: VideoRepository,
    private val requestSearchUpdateByContentPartner: RequestSearchUpdateByContentPartner
) {
    operator fun invoke(contentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)
        val contentPartnerBefore = contentPartnerRepository.findById(contentPartnerId = id)

        if (request.searchable != contentPartnerBefore?.searchable) {
            requestSearchUpdateByContentPartner.invoke(
                id,
                getSearchUpdateRequestType(request.searchable!!)
            )
        }

        val updateCommands = ContentPartnerUpdatesConverter().convert(id, request)

        contentPartnerRepository.update(updateCommands)

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: ${id.value}")

        updateContentPartnerInVideos(contentPartner)

        return contentPartner
    }

    private fun updateContentPartnerInVideos(
        contentPartner: ContentPartner
    ) {
        val videosAffected = videoRepository.findByContentPartnerId(contentPartnerId = contentPartner.contentPartnerId)

        val commands = videosAffected.flatMap { video ->
            listOf(
                VideoUpdateCommand.ReplaceContentPartner(videoId = video.videoId, contentPartner = contentPartner),
                VideoUpdateCommand.ReplaceAgeRange(videoId = video.videoId, ageRange = contentPartner.ageRange)
            )
        }

        videoRepository.bulkUpdate(commands)
    }

    private fun getSearchUpdateRequestType(searchable: Boolean): RequestSearchUpdateByContentPartner.RequestType =
        if (searchable) {
            RequestSearchUpdateByContentPartner.RequestType.INCLUDE
        } else {
            RequestSearchUpdateByContentPartner.RequestType.EXCLUDE
        }
}