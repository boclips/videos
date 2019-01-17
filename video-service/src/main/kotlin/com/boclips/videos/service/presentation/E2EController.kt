package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.video.VideoEntityRepository
import mu.KLogging
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Profile("testing", "test")
@RestController
@RequestMapping("/v1/e2e/actions")
class E2EController(
        private val videoAssetRepository: VideoAssetRepository,
        private val legacySearchIndex: LegacySearchService,
        private val searchService: SearchService,
        private val videoEntityRepository: VideoEntityRepository
) {
    companion object : KLogging()

    @PostMapping("/reset_all")
    fun resetAll(): ResponseEntity<Any> {
        try {
            videoAssetRepository.streamAll { videos ->
                videos.asSequence().forEach { videoAsset ->
                    legacySearchIndex.removeFromSearch(videoAsset.assetId.value)
                    searchService.removeFromSearch(videoAsset.assetId.value)
                }
            }

            videoEntityRepository.deleteAll()

            if (videoEntityRepository.count() != 0L) {
                throw IllegalStateException("Table drop failed")
            }
        } catch (ex: Exception) {
            logger.error { "Failed to reset video-service state" }
        }
        logger.info { "Reset video-service state successfully" }

        return ResponseEntity(HttpStatus.OK)
    }

}