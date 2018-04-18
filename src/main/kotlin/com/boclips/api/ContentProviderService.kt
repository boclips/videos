package com.boclips.api

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux

@Transactional(readOnly = true)
@Service
class ContentProviderService(val videoRepository: VideoRepository, val mongoTemplate: ReactiveMongoTemplate) {

    fun deleteContentProvider(contentProviderName: String): Mono<DeleteResult> {
        val videos = videoRepository.findBySource(contentProviderName)
        return videos.toFlux()
                .flatMap {
                    Flux.concat(
                            mongoTemplate.remove(Query().addCriteria(Criteria.where("reference_id").isEqualTo(it.id)), "videodescriptors"),
                            mongoTemplate.remove(Query().addCriteria(Criteria.where("asset_id").isEqualTo(it.id)), "orderlines")
                    )
                }
                .map { true }.onErrorReturn(false)
                .reduce { a: Boolean, i: Boolean -> a && i }
                .map { success ->
                    if (success) {
                        videoRepository.deleteBySource(contentProviderName)
                        DeleteResult(true, videos.size)
                    } else DeleteResult(false, 0)
                }
    }

}

data class DeleteResult(val success: Boolean, val videosRemoved: Int)