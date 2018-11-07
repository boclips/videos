package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.SearchService
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.domain.service.VideoService
import mu.KLogging
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class MysqlVideoService(
        private val searchService: SearchService,
        private val playbackVideo: PlaybackProvider,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : VideoService {
    companion object : KLogging() {
        private const val DELETE_QUERY = "DELETE FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_QUERY = "SELECT * FROM metadata_orig WHERE id IN (:ids)"
        private const val SELECT_ALL_VIDEOS_QUERY = "SELECT * FROM metadata_orig"
    }

    override fun findVideosBy(query: VideoSearchQuery): List<Video> {
        val videoIds = searchService.search(query.text).map { VideoId(videoId = it) }
        logger.info { "Found ${videoIds.size} videos for query ${query.text}" }
        return findVideosBy(videoIds)
    }

    override fun findVideosBy(videoIds: List<VideoId>): List<Video> {
        val allFoundVideos = findAllById(videoIds.map { videoId -> videoId.videoId.toLong() })
        logger.info { "Found ${videoIds.size} videos for ids $videoIds" }
        return allFoundVideos.map { it.toVideo() }
    }

    override fun findVideoBy(videoId: VideoId): Video {
        val videoOptional = findById(videoId.videoId.toLong())
        logger.info { "Found ${videoOptional.map { 1 }.orElse(0)} video for id $videoId" }
        val videoEntity = videoOptional.orElseThrow { VideoNotFoundException() }

        return videoEntity.toVideo()
    }

    override fun removeVideo(video: Video) {
        searchService.removeFromSearch(video.videoId.videoId)
        logger.info { "Removed video ${video.videoId} from search index" }
        deleteById(video.videoId.videoId.toLong())
        logger.info { "Removed video ${video.videoId} from video repository" }
        playbackVideo.removePlayback(video.playbackId.playbackId)
        logger.info { "Removed video ${video.videoId} from video host" }
    }

    private fun findAllById(ids: List<Long>): List<VideoEntity> {
        if (ids.isEmpty()) {
            return emptyList()
        }

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

    override fun findAllVideos(consumer: (videos: Sequence<Video>) -> Unit) {
        jdbcTemplate.query(SELECT_ALL_VIDEOS_QUERY, StreamingVideoResultExtractor(consumer))
    }

}
