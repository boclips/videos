package com.boclips.videoanalyser.infrastructure

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.service.BoclipsVideoService
import mu.KLogging
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet


@Repository
class BoclipsVideoRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : BoclipsVideoService {
    companion object : KLogging() {
        private const val FIELDS = "id, reference_id, title, source, unique_id, duration, description, date"
    }

    override fun getVideoMetadataByReferenceIds(referenceIds: Collection<String>): Set<BoclipsVideo> {
        if (referenceIds.isEmpty()) {
            return emptySet()
        }

        return jdbcTemplate.query(
                "SELECT $FIELDS FROM metadata_orig where reference_id in (:ids)",
                mapOf("ids" to referenceIds),
                this::mapResultsToBoclipsVideos)
                .toSet()
    }

    override fun countAllVideos() =
            jdbcTemplate.queryForObject("SELECT COUNT(1) FROM metadata_orig", emptyMap<String, Any>(), Int::class.java)
                    ?: 0

    override fun getAllVideos(): Set<BoclipsVideo> {
        logger.info("Getting all videos from mysql")
        return jdbcTemplate.query("SELECT $FIELDS FROM metadata_orig", this::mapResultsToBoclipsVideos)
                .toSet()
    }

    override fun deleteVideos(videos: Set<BoclipsVideo>) {
        videos.asSequence().map { it.id }.chunked(50).toList().forEach {
            jdbcTemplate.update(
                    "DELETE FROM metadata_orig WHERE id in (:ids)",
                    MapSqlParameterSource().apply { addValue("ids", it) }

            )
        }
    }


    private fun mapResultsToBoclipsVideos(resultSet: ResultSet, index: Int): BoclipsVideo {

        return BoclipsVideo(
                id = resultSet.getInt("id"),
                referenceId = resultSet.getString("reference_id"),
                title = resultSet.getString("title"),
                duration = resultSet.getString("duration"),
                description = resultSet.getString("description"),
                date = resultSet.getTimestamp("date")?.toLocalDateTime(),
                contentProvider = resultSet.getString("source"),
                contentProviderId = resultSet.getString("unique_id")
        )
    }
}
