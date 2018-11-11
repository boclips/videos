package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import mu.KLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class MysqlVideoAssetRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : VideoAssetRepository {
    companion object : KLogging() {
        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
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
}
