package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter

class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val requestVideoSearchUpdateByContentPartner: RequestBulkVideoSearchUpdateByContentPartner
) {
    operator fun invoke(contentPartnerId: String, request: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)
        val contentPartnerBefore = contentPartnerRepository.findById(contentPartnerId = id)

        val updateCommands = ContentPartnerUpdatesConverter().convert(id, request)

        contentPartnerRepository.update(updateCommands)

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: ${id.value}")

        val searchable =
            request.hiddenFromSearchForDeliveryMethods?.let { it.map(DeliveryMethodResourceConverter::fromResource) != DeliveryMethod.ALL }
                ?: request.searchable ?: true

        if (searchable != contentPartnerBefore?.searchable) {
            requestVideoSearchUpdateByContentPartner.invoke(
                id,
                getSearchUpdateRequestType(searchable)
            )
        }

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
                VideoUpdateCommand.ReplaceAgeRange(videoId = video.videoId, ageRange = contentPartner.ageRange),
                VideoUpdateCommand.UpdateHiddenFromSearchForDeliveryMethods(
                    videoId = video.videoId,
                    deliveryMethods = contentPartner.hiddenFromSearchForDeliveryMethods
                ),
                if (contentPartner.searchable) {
                    VideoUpdateCommand.MakeSearchable(videoId = video.videoId)
                } else {
                    VideoUpdateCommand.HideFromSearch(videoId = video.videoId)
                }
            )
        }

        videoRepository.bulkUpdate(commands)
    }

    private fun getSearchUpdateRequestType(searchable: Boolean): RequestBulkVideoSearchUpdateByContentPartner.RequestType =
        if (searchable) {
            RequestBulkVideoSearchUpdateByContentPartner.RequestType.INCLUDE
        } else {
            RequestBulkVideoSearchUpdateByContentPartner.RequestType.EXCLUDE
        }
}