package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.service.BoclipsVideoService
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet


@Repository
class BoclipsVideoRepository(val jdbcTemplate: NamedParameterJdbcTemplate) : BoclipsVideoService {

    companion object {
        private const val FIELDS = "id, reference_id, title, source, unique_id, duration, description, date"
    }

    override fun getVideoMetadata(ids: Collection<String>): Set<BoclipsVideo> {
        val numericIds = ids.filter { it.toIntOrNull() != null }
        val output = mutableSetOf<BoclipsVideo>()
        if (numericIds.isNotEmpty()) {
            output += jdbcTemplate.query(
                    "SELECT $FIELDS FROM metadata_orig where id in (:ids) and reference_id is null",
                    mapOf("ids" to numericIds),
                    this::mapResultsToBoclipsVideos)
                    .toSet()
        }

        return output + jdbcTemplate.query(
                "SELECT $FIELDS FROM metadata_orig where reference_id in (:ids)",
                mapOf("ids" to ids),
                this::mapResultsToBoclipsVideos)
                .toSet()

    }

    override fun countAllVideos() =
            jdbcTemplate.queryForObject("SELECT COUNT(1) FROM metadata_orig", emptyMap<String, Any>(), Int::class.java)
                    ?: 0

    override fun getAllVideos() = jdbcTemplate.query("SELECT $FIELDS FROM metadata_orig", this::mapResultsToBoclipsVideos)
            .toSet()

    private fun mapResultsToBoclipsVideos(resultSet: ResultSet, index: Int): BoclipsVideo {
        val referenceId = resultSet.getString("reference_id")

        return BoclipsVideo(
                id = resultSet.getInt("id").toString(),
                referenceId = referenceId,
                title = resultSet.getString("title"),
                duration = resultSet.getString("duration"),
                description = resultSet.getString("description"),
                date = resultSet.getTimestamp("date")?.toLocalDateTime(),
                contentProvider = resultSet.getString("source"),
                contentProviderId = resultSet.getString("unique_id")
        )
    }
}
