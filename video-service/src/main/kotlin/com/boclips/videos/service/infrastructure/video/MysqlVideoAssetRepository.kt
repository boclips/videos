package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.video.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.video.subject.VideoSubjectEntity
import mu.KLogging
import javax.transaction.Transactional

open class MysqlVideoAssetRepository(
        private val subjectRepository: SubjectRepository,
        private val videoRepository: VideoEntityRepository
) : VideoAssetRepository {
    companion object : KLogging();

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        if (assetIds.isEmpty()) {
            return emptyList()
        }

        val videoIds = assetIds.map { it.value }
        val videoEntities = findAllByIdWhilstRetainingOrder(videoIds)
        val videoIdsToSubjects = getSubjectsByVideoIds(videoIds)

        logger.info { "Found ${videoEntities.size} videos for assetIds $assetIds" }
        return videoEntities.map { it.toVideoAsset().copy(subjects = videoIdsToSubjects[it.id].orEmpty().toSet()) }
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

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val videoEntity = VideoEntity.fromVideoAsset(videoAsset)
        val savedVideoEntity = videoRepository.save(videoEntity)

        val subjects = videoAsset.subjects.map { subject -> VideoSubjectEntity(savedVideoEntity.id, subject.name) }
        subjectRepository.add(subjects)

        logger.info { "Created video ${savedVideoEntity.id}" }
        return savedVideoEntity.toVideoAsset()
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
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

    private fun findAllByIdWhilstRetainingOrder(videoIds: List<String>): List<VideoEntity> = videoIds
            .mapIndexed { index, id -> id.toLong() to index }
            .toMap()
            .let { indexById ->
                videoRepository.findAllByIdIn(videoIds.map { it.toLong() }).sortedBy { indexById[it.id] }
            }

    private fun getSubjectsByVideoIds(videoIds: List<String>): Map<Long, List<Subject>> =
            subjectRepository.findByVideoIds(videoIds.map { it.toLong() })
                    .groupBy({ it.videoId!! }, { Subject(it.subjectName!!) })
}
