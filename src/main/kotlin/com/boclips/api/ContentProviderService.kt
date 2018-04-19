package com.boclips.api

import mu.KLogging
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
    companion object : KLogging()

    fun deleteContentProvider(contentProviderName: String): Mono<DeleteResult> {
        val videos = videoRepository.findBySource(contentProviderName)
        logger.info { "attempting to remove $videos videos" }
        return videos.toFlux()
                .flatMap {
                    Flux.concat(
                            mongoTemplate.remove(Query().addCriteria(Criteria.where("reference_id").isEqualTo(it.id)), "videodescriptors")
                                    .map { DeleteResult(playlistEntriesRemoved = it.deletedCount) },
                            mongoTemplate.remove(Query().addCriteria(Criteria.where("asset_id").isEqualTo(it.id)), "orderlines")
                                    .map { DeleteResult(orderlinesEntriesRemoved = it.deletedCount) }
                    )
                }
                .onErrorReturn(DeleteResult(success = false))
                .reduce { a: DeleteResult, i: DeleteResult -> a.merge(i) }
                .map { result ->
                    if (result.success) {
                        videoRepository.deleteBySource(contentProviderName)
                        result.copy(videosRemoved = videos.size)
                    } else result
                }
    }
}

data class DeleteResult(val success: Boolean = true, val videosRemoved: Int = 0, val playlistEntriesRemoved: Long = 0, val orderlinesEntriesRemoved: Long = 0) {

    fun merge(other: DeleteResult) = DeleteResult(
            success = success && other.success,
            videosRemoved = videosRemoved + other.videosRemoved,
            playlistEntriesRemoved = playlistEntriesRemoved + other.playlistEntriesRemoved,
            orderlinesEntriesRemoved = orderlinesEntriesRemoved + other.orderlinesEntriesRemoved
    )
}