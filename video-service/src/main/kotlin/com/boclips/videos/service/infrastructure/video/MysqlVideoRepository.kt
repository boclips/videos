package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.VideoRepository
import mu.KLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class MysqlVideoRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : VideoRepository {

    companion object : KLogging() {
        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
    }

    override fun findVideosBy(videoIds: List<VideoId>): List<Video> {
        if (videoIds.isEmpty()) {
            return emptyList()
        }

        val videoEntities = jdbcTemplate.query(SELECT_QUERY, MapSqlParameterSource("ids", videoIds.map { it.videoId }), rowMapper)
        logger.info { "Found ${videoIds.size} videos for videoIds $videoIds" }
        return videoEntities.map { it.toVideo() }
    }

    override fun findVideoBy(videoId: VideoId): Video? {
        return try {
            jdbcTemplate.queryForObject(SELECT_QUERY, MapSqlParameterSource("ids", videoId.videoId), rowMapper)!!.toVideo()
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    override fun findAllVideos(consumer: (videos: Sequence<Video>) -> Unit) {
        jdbcTemplate.query(SELECT_ALL_VIDEOS_QUERY, StreamingVideoResultExtractor(consumer))
    }

    override fun deleteVideoById(videoId: VideoId) {
        jdbcTemplate.update(DELETE_QUERY, MapSqlParameterSource("ids", videoId.videoId))
    }
}
