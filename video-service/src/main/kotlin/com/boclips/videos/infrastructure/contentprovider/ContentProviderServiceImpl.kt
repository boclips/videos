package com.boclips.videos.infrastructure.contentprovider

import com.boclips.videos.domain.model.ContentProvider
import com.boclips.videos.domain.model.DeleteResult
import com.boclips.videos.domain.services.ContentProviderService
import com.boclips.videos.infrastructure.videos.VideoRepository
import mu.KLogging
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Transactional(readOnly = true)
class ContentProviderServiceImpl(
        val videoRepository: VideoRepository,
        val contentProviderRepository: ContentProviderRepository,
        val mongoTemplate: ReactiveMongoTemplate
) : ContentProviderService {
    companion object : KLogging()

    override fun deleteByName(contentProviderName: String): Mono<DeleteResult> {
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

    override fun getAll(): Flux<ContentProvider> {
        return contentProviderRepository.findAll().map { it.toContentProvider() }
    }

    override fun create(name: String): Mono<Boolean> {
        return contentProviderRepository.findByName(name)
                .map { false }
                .defaultIfEmpty(true)
                .filter { it }
                .flatMap {
                    val now = ZonedDateTime.now(ZoneOffset.UTC).toString()
                    contentProviderRepository.save(ContentProviderEntity(name = name, dateCreated = now, dateUpdated = now, uuid = UUID.randomUUID().toString()))
                            .map { true }
                }
                .defaultIfEmpty(false)
    }

    override fun getById(contentProviderId: String): Mono<ContentProvider> {
        return contentProviderRepository.findById(contentProviderId).map { it.toContentProvider() }
    }

}