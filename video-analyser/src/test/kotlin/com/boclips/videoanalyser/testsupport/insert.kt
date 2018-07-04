package com.boclips.videoanalyser.testsupport

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MetadataTestRepository(val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun insert(
            id: String? = null,
            referenceId: String? = null,
            title: String? = "some title",
            contentProvider: String? = "some cp",
            contentProviderId: String? = "some cp id",
            description: String? = null,
            duration: String? = null,
            date: LocalDateTime? = null
    ) {
        jdbcTemplate.update("""
            INSERT INTO metadata_orig(id, reference_id, title, source, unique_id, description, duration, date)
            VALUES(
                :id,
                :referenceId,
                :title,
                :contentProvider,
                :contentProviderId,
                :description,
                :duration,
                :date)
            """, mapOf(
                "id" to id,
                "referenceId" to referenceId,
                "title" to title,
                "contentProvider" to contentProvider,
                "contentProviderId" to contentProviderId,
                "description" to description,
                "duration" to duration,
                "date" to date
        ))
    }

}
