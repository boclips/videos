package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoDetails
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.VideoLibrary
import mu.KLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class MysqlVideoLibrary(private val jdbcTemplate: NamedParameterJdbcTemplate) : VideoLibrary {

    companion object : KLogging() {
        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
    }

    override fun findVideosBy(videoIds: List<VideoId>): List<VideoDetails> {
        if (videoIds.isEmpty()) {
            return emptyList()
        }

        val videoEntities = jdbcTemplate.query(SELECT_QUERY, MapSqlParameterSource("ids", videoIds.map { it.value }), rowMapper)
        logger.info { "Found ${videoIds.size} videos for videoIds $videoIds" }
        return videoEntities.map { it.toVideoDetails() }
    }

    override fun findVideoBy(videoId: VideoId): VideoDetails? {
        return try {
            jdbcTemplate.queryForObject(SELECT_QUERY, MapSqlParameterSource("ids", videoId.value), rowMapper)!!.toVideoDetails()
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    override fun findAllVideos(consumer: (videos: Sequence<VideoDetails>) -> Unit) {
        jdbcTemplate.query(SELECT_ALL_VIDEOS_QUERY, StreamingVideoResultExtractor(consumer))
    }

    override fun deleteVideoBy(videoId: VideoId) {
        jdbcTemplate.update(DELETE_QUERY, MapSqlParameterSource("ids", videoId.value))
    }
}
