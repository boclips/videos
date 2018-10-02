package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoService
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class MysqlVideoService(
        private val searchService: SearchService,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : VideoService {
    private val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
    private val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"

    override fun findVideosBy(query: VideoSearchQuery): List<Video> {
        val videoIds = searchService.search(query.text)
        return findVideosBy(videoIds)
    }

    override fun findVideosBy(videoIds: List<VideoId>): List<Video> {
        val allFoundVideos = findAllById(videoIds.map { videoId -> videoId.videoId.toLong() })
        return allFoundVideos.map { videoEntity -> convertToVideo(videoEntity) }
    }

    override fun findVideoBy(videoId: VideoId): Video {
        val videoOptional = findById(videoId.videoId.toLong())
        val videoEntity = videoOptional.orElseThrow { VideoNotFoundException() }

        return convertToVideo(videoEntity)
    }

    override fun removeVideo(video: Video) {
        searchService.removeFromSearch(video.videoId)
        deleteById(video.videoId.videoId.toLong())
    }

    private fun convertToVideo(videoEntity: VideoEntity): Video {
        return Video(
                videoId = VideoId(videoId = videoEntity.id.toString(), referenceId = videoEntity.reference_id),
                title = videoEntity.title!!,
                duration = Duration.between(
                        LocalTime.MIN,
                        LocalTime.parse(videoEntity.duration!!)
                ),
                description = videoEntity.description!!,
                releasedOn = LocalDate.parse(videoEntity.date!!),
                contentProvider = videoEntity.source!!,
                videoPlayback = null
        )
    }

    private fun findAllById(ids: List<Long>): List<VideoEntity> {
        val parameters = MapSqlParameterSource()
        parameters.addValue("ids", ids)

        return jdbcTemplate.query(SELECT_QUERY, parameters, rowMapper)
    }

    private fun findById(videoId: Long): Optional<VideoEntity> {
        val parameters = MapSqlParameterSource()
        parameters.addValue("ids", videoId)

        return try {
            val videoEntityOrNull = jdbcTemplate.queryForObject(SELECT_QUERY, parameters, rowMapper)
            Optional.ofNullable(videoEntityOrNull)
        } catch (ex: Exception) {
            Optional.empty()
        }
    }

    private fun deleteById(videoId: Long) {
        val parameters = MapSqlParameterSource()
        parameters.addValue("ids", videoId)

        jdbcTemplate.update(DELETE_QUERY, parameters)
    }
}