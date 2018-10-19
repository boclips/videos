package com.boclips.videos.service.infrastructure.video

import com.boclips.search.service.domain.VideoMetadata
import java.sql.ResultSet

object VideoMetadataRowMapper {
    fun mapRow(row: ResultSet): VideoMetadata {
        val id = row.getLong("id")
        val title = row.getString("title")
        val description = row.getString("description")
        val keywords = row.getString("keywords")
        return VideoMetadata(id = id.toString(), title = title, description = description, keywords = keywords.split(','))
    }
}