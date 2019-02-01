package com.boclips.videos.service.infrastructure.video.mysql

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.exceptions.ResourceNotFoundException
import com.boclips.videos.service.infrastructure.video.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.video.subject.VideoSubjectEntity
import mu.KLogging
import org.springframework.transaction.annotation.Transactional
import java.lang.UnsupportedOperationException

open class MysqlVideoAssetRepository(
        private val subjectRepository: SubjectRepository,
        private val videoRepository: VideoEntityRepository,
        private val videoSequenceReader: VideoSequenceReader
) : VideoAssetRepository {
    companion object : KLogging();

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        if (assetIds.isEmpty()) {
            return emptyList()
        }

        val videoIds = assetIds.mapNotNull { it.value.toLongOrNull() }
        val videoEntities = findAllByIdWhilstRetainingOrder(videoIds)
        val videoIdsToSubjects = getSubjectsByVideoIds(videoIds)

        logger.info { "Found ${videoEntities.size} videos for assetIds $assetIds" }
        return videoEntities.map { it.toVideoAsset().copy(subjects = videoIdsToSubjects[it.id].orEmpty().toSet()) }
    }

    override fun find(assetId: AssetId): VideoAsset? {
        return findAll(listOf(assetId)).firstOrNull()
    }

    override fun streamAll(consumer: (Sequence<VideoAsset>) -> Unit) {
        videoSequenceReader.readOnly { sequence ->
            consumer(sequence.mapNotNull(this::convertToVideoAssetOrNull))
        }
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val videoEntity = VideoEntity.fromVideoAsset(videoAsset)
        val savedVideoEntity = videoRepository.save(videoEntity)

        val subjects = videoAsset.subjects.map { subject -> VideoSubjectEntity(savedVideoEntity.id, subject.name) }
        subjectRepository.add(subjects)

        logger.info { "Created video ${savedVideoEntity.id}" }
        return savedVideoEntity.toVideoAsset()
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        if (find(videoAsset.assetId) == null) {
            throw ResourceNotFoundException()
        }

        val savedVideoEntity = videoRepository.save(VideoEntity.fromVideoAsset(videoAsset))

        val videoId = videoAsset.assetId.value.toLong()
        val newSubjectNames = videoAsset.subjects.map { subject -> subject.name }
        subjectRepository.setSubjectsForVideo(videoId, newSubjectNames)

        logger.info { "Updated video ${savedVideoEntity.id}" }
        return videoAsset
    }

    override fun delete(assetId: AssetId) {
        videoRepository.deleteById(assetId.value.toLong())
        logger.info { "Deleted video $assetId" }
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        return videoRepository.countBySourceAndUniqueId(contentPartnerId, partnerVideoId) > 0
    }

    @Transactional
    override fun disableFromSearch(assetIds: List<AssetId>) {
        videoRepository.setSearchableByIdIn(false, assetIds.map { it.value.toLong() })
    }

    @Transactional
    override fun makeSearchable(assetIds: List<AssetId>) {
        videoRepository.setSearchableByIdIn(true, assetIds.map { it.value.toLong() })
    }

    private fun findAllByIdWhilstRetainingOrder(videoIds: List<Long>): List<VideoEntity> = videoIds
            .mapIndexed { index, id -> id to index }
            .toMap()
            .let { indexById ->
                videoRepository.findAllByIdIn(videoIds).sortedBy { indexById[it.id] }
            }

    private fun getSubjectsByVideoIds(videoIds: List<Long>): Map<Long, List<Subject>> =
            subjectRepository.findByVideoIds(videoIds)
                    .groupBy({ it.videoId!! }, { Subject(it.subjectName!!) })

    override fun resolveAlias(alias: String): AssetId? {
        throw UnsupportedOperationException("Alias resolution not supported by mysql video repository")
    }

    private fun convertToVideoAssetOrNull(it: VideoEntity): VideoAsset? {
        return try {
            it.toVideoAsset()
        } catch (e: Exception) {
            logger.error(e) { "Could not convert video: ${it.id}" }
            null
        }
    }
}
