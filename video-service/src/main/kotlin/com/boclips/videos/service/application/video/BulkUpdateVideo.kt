package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import mu.KLogging

class BulkUpdateVideo(private val videoRepository: VideoRepository) {
    companion object : KLogging();

    operator fun invoke(bulkUpdateRequest: BulkUpdateRequest?) {
        bulkUpdateRequest?.distributionMethods?.let {
            val videoIds = bulkUpdateRequest.ids.map(::VideoId)

            val commands = videoIds.map { videoId ->
                VideoUpdateCommand.ReplaceDistributionMethods(
                    videoId = videoId,
                    distributionMethods = bulkUpdateRequest.distributionMethods.map {
                        DistributionMethodResourceConverter.fromResource(it)
                    }.toSet()
                )
            }

            videoRepository.bulkUpdate(commands)

        } ?: throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
    }
}
