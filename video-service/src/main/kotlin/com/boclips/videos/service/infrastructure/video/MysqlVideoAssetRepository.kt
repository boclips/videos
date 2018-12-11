package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import mu.KLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder


class MysqlVideoAssetRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : VideoAssetRepository {
    companion object : KLogging() {

        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
        private const val CREATE_QUERY = """INSERT INTO metadata_orig (reference_id, source, unique_id, namespace, title, description, `date`, duration, keywords, restrictions, type_id, playback_id, playback_provider)
                        VALUES (:reference_id, :contentPartnerId, :contentPartnerVideoId, :namespace, :title, :description, :releasedOn, :duration, :keywords, :legalRestrictions, :content_type, :playback_id, :playback_provider);"""
        private const val COUNT_VIDEOS_WITH_CONTENT_PARTNER_ID_QUERY = "SELECT COUNT(1) FROM metadata_orig WHERE source = :contentPartnerId AND unique_id = :partnerVideoId"
        private const val UPDATE_VIDEO_QUERY = "UPDATE metadata_orig SET title = :title, description = :description, playback_id = :playback_id, keywords = :keywords, `date` = :releasedOn, source = :contentPartnerId, unique_id = :contentPartnerVideoId, type_id = :content_type, duration = :duration, restrictions = :legalRestrictions, playback_provider = :playback_provider WHERE id = :id"
        private const val UPDATE_SUBJECTS_QUERY = "INSERT INTO video_subject(video_id, subject_name) values (:video_id, :subject_name)"
        private const val SELECT_SUBJECT_QUERY = "SELECT * FROM video_subject WHERE video_id IN (:video_ids)"
    }

    override fun update(videoAsset: VideoAsset): VideoAsset {
        jdbcTemplate.update(UPDATE_VIDEO_QUERY, queryParamsForVideo(videoAsset))

        videoAsset.subjects.map { it.name }.forEach { subject ->
            jdbcTemplate.update(UPDATE_SUBJECTS_QUERY, mapOf("video_id" to videoAsset.assetId.value.toInt(), "subject_name" to subject))
        }

        return videoAsset
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        if (assetIds.isEmpty()) {
            return emptyList()
        }

        val videoIds = assetIds.map { it.value }
        val videoEntities = jdbcTemplate.query(
                SELECT_QUERY + " ORDER BY ${sqlOrderIds(assetIds)}",
                MapSqlParameterSource("ids", videoIds),
                rowMapper
        )
        val videoIdsToSubjects = getSubjectsByVideoIds(videoIds)

        logger.info { "Found ${assetIds.size} videos for assetIds $assetIds" }
        return videoEntities.map { it.toVideoAsset().copy(subjects = videoIdsToSubjects[it.id].orEmpty().toSet()) }
    }

    private fun getSubjectsByVideoIds(videoIds: List<String>): Map<Long, List<Subject>> =
            jdbcTemplate.query(SELECT_SUBJECT_QUERY, mapOf("video_ids" to videoIds)) { resultSet, _ ->
                resultSet.getLong("video_id") to Subject(resultSet.getString("subject_name"))
            }.groupBy({ it.first }, { it.second })


    override fun find(assetId: AssetId): VideoAsset? {
        return findAll(listOf(assetId)).firstOrNull()
    }

    override fun streamAll(consumer: (videos: Sequence<VideoAsset>) -> Unit) {
        jdbcTemplate.query(SELECT_ALL_VIDEOS_QUERY, StreamingVideoResultExtractor(consumer))
    }

    override fun delete(assetId: AssetId) {
        jdbcTemplate.update(DELETE_QUERY, MapSqlParameterSource("ids", assetId.value))
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val params = queryParamsForVideo(videoAsset)

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(CREATE_QUERY, MapSqlParameterSource(params), keyHolder)

        val id = keyHolder.key!!

        logger.info { "Persisted video $id" }
        return find(AssetId(id.toString()))!!
    }

    private fun queryParamsForVideo(videoAsset: VideoAsset): MutableMap<String, Any?> {
        val params = mutableMapOf<String, Any?>()
        params["contentPartnerId"] = videoAsset.contentPartnerId
        params["contentPartnerVideoId"] = videoAsset.contentPartnerVideoId
        params["namespace"] = generateNamespace(videoAsset.contentPartnerId, videoAsset.contentPartnerVideoId)
        params["title"] = videoAsset.title
        params["description"] = videoAsset.description
        params["releasedOn"] = videoAsset.releasedOn
        params["duration"] = DurationFormatUtils.formatDuration(videoAsset.duration.toMillis(), "HH:mm:ss", true)
        params["legalRestrictions"] = videoAsset.legalRestrictions
        params["keywords"] = videoAsset.keywords.joinToString()
        params["content_type"] = videoAsset.type.id
        params["reference_id"] = videoAsset.playbackId.value
        params["playback_id"] = videoAsset.playbackId.value
        params["playback_provider"] = videoAsset.playbackId.type.name
        params["id"] = videoAsset.assetId.value
        return params
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        val params = mutableMapOf<String, Any?>()
        params["contentPartnerId"] = contentPartnerId
        params["partnerVideoId"] = partnerVideoId

        return jdbcTemplate.queryForObject(COUNT_VIDEOS_WITH_CONTENT_PARTNER_ID_QUERY, MapSqlParameterSource(params), Integer::class.java)!! > 0
    }

    private fun sqlOrderIds(assetIds: List<AssetId>) =
            assetIds.map { "id='${it.value}' DESC" }.joinToString()
}
