package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.SolrDocumentNotFound
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.video.mysql.VideoEntityRepository
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
        private val legacySearchIndex: LegacySearchService,
        private val searchService: SearchService,
        private val videoEntityRepository: VideoEntityRepository
) {
    companion object : KLogging()

    @PostMapping("/reset_all")
    fun resetAll(): ResponseEntity<Any> {
        try {
            videoEntityRepository.findAll().forEach { videEntity ->
                val videoId = videEntity.id.toString()
                try {
                    legacySearchIndex.removeFromSearch(videoId)
                } catch (ex: SolrDocumentNotFound) {
                    logger.warn { "Could not find and delete video $videoId in SOLR" }
                }

                try {
                    searchService.removeFromSearch(videoId)
                } catch(ex: Exception) {
                    logger.warn { "Could not find and delete video $videoId in ES" }
                }

                logger.info { "Finished attempt to reset video $videoId" }
            }

            videoEntityRepository.deleteAll()

            if (videoEntityRepository.count() != 0L) {
                throw IllegalStateException("Table drop failed")
            }
        } catch (ex: Exception) {
            logger.error { "Failed to reset video-service state" }
            throw IllegalStateException("Failed to reset video-service state", ex)
        }

        return ResponseEntity(HttpStatus.OK)
    }

}