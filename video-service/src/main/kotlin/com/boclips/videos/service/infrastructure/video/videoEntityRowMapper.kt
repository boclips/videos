package com.boclips.videos.service.infrastructure.video

import java.sql.ResultSet

val rowMapper = { resultSet: ResultSet, _: Int ->
    VideoEntity(
            id = resultSet.getLong("id"),
            title = resultSet.getString("title"),
            source = resultSet.getString("source"),
            namespace = resultSet.getString("namespace"),
            description = resultSet.getString("description"),
            date = resultSet.getString("date"),
            duration = resultSet.getString("duration"),
            keywords = resultSet.getString("keywords"),
            price_category = resultSet.getString("price_category"),
            sounds = resultSet.getString("sounds"),
            color = resultSet.getString("color"),
            location = resultSet.getString("location"),
            country = resultSet.getString("country"),
            state = resultSet.getString("state"),
            city = resultSet.getString("city"),
            region = resultSet.getString("region"),
            alternative_id = resultSet.getString("alternative_id"),
            alt_source = resultSet.getString("alt_source"),
            restrictions = resultSet.getString("restrictions"),
            type_id = resultSet.getInt("type_id"),
            reference_id = resultSet.getString("reference_id")
    )
}