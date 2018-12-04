package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import mu.KLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.apache.commons.lang3.time.DurationFormatUtils


class MysqlVideoAssetRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : VideoAssetRepository {
    companion object : KLogging() {
        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
        private const val CREATE_QUERY = """INSERT INTO metadata_orig (reference_id, source, unique_id, namespace, title, description, `date`, duration, keywords, restrictions, type_id, playback_id, playback_provider)
                        VALUES (:reference_id, :provider, :providerVideoId, :namespace, :title, :description, :releasedOn, :duration, :keywords, :legalRestrictions, :content_type, :playback_id, :playback_provider);"""
    }
    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        if (assetIds.isEmpty()) {
            return emptyList()
        }

        val videoEntities = jdbcTemplate.query(SELECT_QUERY, MapSqlParameterSource("ids", assetIds.map { it.value }), rowMapper)
        logger.info { "Found ${assetIds.size} videos for assetIds $assetIds" }
        return videoEntities.map { it.toVideoAsset() }
    }

    override fun find(assetId: AssetId): VideoAsset? {
        return try {
            jdbcTemplate.queryForObject(SELECT_QUERY, MapSqlParameterSource("ids", assetId.value), rowMapper)!!.toVideoAsset()
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    override fun streamAll(consumer: (videos: Sequence<VideoAsset>) -> Unit) {
        jdbcTemplate.query(SELECT_ALL_VIDEOS_QUERY, StreamingVideoResultExtractor(consumer))
    }

    override fun delete(assetId: AssetId) {
        jdbcTemplate.update(DELETE_QUERY, MapSqlParameterSource("ids", assetId.value))
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val params = mutableMapOf<String, Any?>()
        params["provider"] = videoAsset.contentProvider
        params["providerVideoId"] = videoAsset.contentProviderId
        params["namespace"] = generateNamespace(videoAsset.contentProvider, videoAsset.contentProviderId)
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

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(CREATE_QUERY, MapSqlParameterSource(params), keyHolder)

        val id = keyHolder.key!!

        logger.info { "Persisted video $id" }
        return find(AssetId(id.toString()))!!
    }
}
