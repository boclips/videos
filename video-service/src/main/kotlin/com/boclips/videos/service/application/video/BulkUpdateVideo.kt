package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import mu.KLogging

open class BulkUpdateVideo(
    private val videoRepository: VideoRepository,
    private val writeSearchService: WriteSearchService<Video>,
    private val legacySearchService: LegacySearchService,
    private val videoAccessService: VideoAccessService
) {

    companion object : KLogging();

    open operator fun invoke(bulkUpdateRequest: BulkUpdateRequest?) {
        when (bulkUpdateRequest?.status) {
            VideoResourceStatus.SEARCHABLE -> makeSearchable(bulkUpdateRequest)
            VideoResourceStatus.SEARCH_DISABLED -> disableFromSearch(bulkUpdateRequest)
            null -> throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
        }
    }

    private fun disableFromSearch(bulkUpdateRequest: BulkUpdateRequest) {
        val videoIds = bulkUpdateRequest.ids.map { VideoId(value = it) }

        videoAccessService.revokeAccess(videoIds)
        writeSearchService.bulkRemoveFromSearch(bulkUpdateRequest.ids)
        legacySearchService.bulkRemoveFromSearch(bulkUpdateRequest.ids)
    }

    private fun makeSearchable(bulkUpdateRequest: BulkUpdateRequest) {
        val videoIds = bulkUpdateRequest.ids.map { VideoId(value = it) }
        videoAccessService.grantAccess(videoIds)

        videoRepository.findAll(videoIds).let { videos ->
            writeSearchService.upsert(videos.asSequence())

            legacySearchService.upsert(videos
                .filter { it.isBoclipsHosted() }
                .map { video -> VideoToLegacyVideoMetadataConverter.convert(video) }
                .asSequence())
        }
    }
}
