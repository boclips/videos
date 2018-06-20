package com.boclips.cleanser.infrastructure.boclips

import com.boclips.cleanser.domain.service.BoclipsVideoService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class BoclipsVideosRepository(val jdbcTemplate: JdbcTemplate) : BoclipsVideoService {
    override fun countAllVideos(): Int {
        val count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM metadata_orig", Int::class.java)
        return count ?: 0
    }

    override fun getAllVideos() = jdbcTemplate.query("SELECT id, reference_id FROM metadata_orig")
    { resultSet: ResultSet, _ ->
        val referenceId = resultSet.getString("reference_id")

        if (referenceId.isNullOrBlank()) {
            resultSet.getInt("id").toString()
        } else {
            referenceId
        }
    }.toSet()
}
