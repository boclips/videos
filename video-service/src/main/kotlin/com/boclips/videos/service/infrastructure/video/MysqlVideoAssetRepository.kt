package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import mu.KLogging
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import javax.transaction.Transactional

@Repository
interface VideoEntityRepository : CrudRepository<VideoEntity, Long> {
    fun countBySourceAndUniqueId(contentPartnerId: String, partnerVideoId: String): Int

    @Query("select * from metadata_orig", nativeQuery = true)
    fun readAll(): Stream<VideoEntity>

    fun findAllByIdIn(ids: List<Long>): List<VideoEntity>
}

open class MysqlVideoAssetRepository(
        private val videoRepository: VideoEntityRepository
) : VideoAssetRepository {
    companion object : KLogging();

    override fun update(videoAsset: VideoAsset): VideoAsset {
        videoRepository.save(VideoEntity.fromVideoAsset(videoAsset))
        return videoAsset
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        if (assetIds.isEmpty()) {
            return emptyList()
        }

        val videoIds = assetIds.map { it.value }

        val videoEntities = findAllByIdWhilstRetainingOrder(videoIds)


        logger.info { "Found ${videoEntities.size} videos for assetIds $assetIds" }
        return videoEntities.map { it.toVideoAsset() }
    }

    override fun find(assetId: AssetId): VideoAsset? {
        return findAll(listOf(assetId)).firstOrNull()
    }

    @Transactional
    override fun streamAll(consumer: (videos: Sequence<VideoAsset>) -> Unit) {
        videoRepository.readAll().use { stream ->
            consumer(stream.iterator().asSequence().map { it.toVideoAsset() })
        }
    }

    override fun delete(assetId: AssetId) {
        videoRepository.deleteById(assetId.value.toLong())
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val entity = videoRepository.save(VideoEntity.fromVideoAsset(videoAsset))

        logger.info { "Persisted video ${entity.id}" }
        return entity.toVideoAsset()
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        return videoRepository.countBySourceAndUniqueId(contentPartnerId, partnerVideoId) > 0
    }

    private fun findAllByIdWhilstRetainingOrder(videoIds: List<String>): List<VideoEntity> = videoIds
            .mapIndexed { index, id -> id.toLong() to index }
            .toMap()
            .let { indexById ->
                videoRepository.findAllByIdIn(videoIds.map { it.toLong() }).sortedBy { indexById[it.id] }
            }
}
