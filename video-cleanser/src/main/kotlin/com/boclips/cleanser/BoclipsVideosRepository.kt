package com.boclips.cleanser

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class BoclipsVideosRepository(val jdbcTemplate: JdbcTemplate) {
    fun getAllIds() = jdbcTemplate.query(
            "SELECT id FROM metadata_orig WHERE reference_id IS NULL",
            { resultSet: ResultSet, _ ->
                resultSet.getInt("id")
            }).toSet()
}
