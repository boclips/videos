package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.VideoAssetToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import org.springframework.transaction.annotation.Transactional

open class BulkUpdate(
        private val videoAssetRepository: VideoAssetRepository,
        private val searchAdminService: GenericSearchServiceAdmin<VideoAsset>,
        private val legacySearchService: LegacySearchService

) {

    @Transactional
    open operator fun invoke(bulkUpdateRequest: BulkUpdateRequest?) {
        when (bulkUpdateRequest?.status) {
            VideoResourceStatus.SEARCHABLE -> makeSearchable(bulkUpdateRequest)
            VideoResourceStatus.SEARCH_DISABLED -> disableFromSearch(bulkUpdateRequest)
            null -> throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
        }
    }

    private fun disableFromSearch(bulkUpdateRequest: BulkUpdateRequest) {
        videoAssetRepository.disableFromSearch(bulkUpdateRequest.ids.map { AssetId(value = it) })

        bulkUpdateRequest.ids.forEach {
            searchAdminService.removeFromSearch(it)
            legacySearchService.removeFromSearch(it)
        }
    }

    private fun makeSearchable(bulkUpdateRequest: BulkUpdateRequest) {
        val assetIds = bulkUpdateRequest.ids.map { AssetId(value = it) }
        videoAssetRepository.makeSearchable(assetIds)

        videoAssetRepository.findAll(assetIds).let {
            searchAdminService.upsert(it.asSequence())

            legacySearchService.upsert(it
                    .filter { asset -> asset.playbackId.type == PlaybackProviderType.KALTURA }
                    .map { asset -> VideoAssetToLegacyVideoMetadataConverter.convert(asset) }
                    .asSequence())
        }
    }
}