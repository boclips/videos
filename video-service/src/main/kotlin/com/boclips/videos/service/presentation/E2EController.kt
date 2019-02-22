package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.SolrDocumentNotFound
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.infrastructure.video.mongo.VideoDocument
import com.boclips.videos.service.infrastructure.video.mongo.VideoDocumentConverter
import com.mongodb.MongoClient
import mu.KLogging
import org.litote.kmongo.getCollection
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
    private val mongoClient: MongoClient
) {
    companion object : KLogging()

    @PostMapping("/reset_all")
    fun resetAll(): ResponseEntity<Any> {
        try {
            mongoClient.getDatabase("video-service-db").getCollection<VideoDocument>("videos").find().forEach { document ->
                val videoId = document.id.toHexString()
                try {
                    legacySearchIndex.removeFromSearch(videoId)
                } catch (ex: SolrDocumentNotFound) {
                    logger.warn { "Could not find and delete video $videoId in SOLR" }
                }

                try {
                    searchService.removeFromSearch(videoId)
                } catch (ex: Exception) {
                    logger.warn { "Could not find and delete video $videoId in ES" }
                }

                logger.info { "Finished attempt to reset video $videoId" }
            }

            mongoClient.getDatabase("video-service-db").getCollection("videos").drop()
        } catch (ex: Exception) {
            logger.error { "Failed to reset video-service state" }
            throw IllegalStateException("Failed to reset video-service state", ex)
        }

        return ResponseEntity(HttpStatus.OK)
    }
}