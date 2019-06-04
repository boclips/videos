package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
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
        val ageRange = request.ageRange?.let { AgeRange.bounded(min = it.min, max = it.max) } ?: AgeRange.unbounded()

        val contentPartner = contentPartnerRepository.update(
            ContentPartner(
                contentPartnerId = ContentPartnerId(value = contentPartnerId),
                name = request.name,
                ageRange = ageRange
            )
        )

        updateContentPartnerInVideos(contentPartner)

        return contentPartner
    }

    private fun updateContentPartnerInVideos(
        contentPartner: ContentPartner
    ) {
        val videosAffected = videoRepository.findByContentPartner(contentPartnerName = contentPartner.name)

        val commands = videosAffected.flatMap { video ->
            listOf(
                VideoUpdateCommand.ReplaceContentPartner(videoId = video.videoId, contentPartner = contentPartner),
                VideoUpdateCommand.ReplaceAgeRange(videoId = video.videoId, ageRange = contentPartner.ageRange)
            )
        }

        videoRepository.bulkUpdate(commands)
    }
}