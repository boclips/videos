package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoDetails
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

class StreamingVideoResultExtractor(val consumer: (videos: Sequence<VideoDetails>) -> Unit) : ResultSetExtractor<Unit> {
    override fun extractData(resultSet: ResultSet) {
        val entities = generateSequence {
            if (resultSet.next()) rowMapper(resultSet, -1) else null
        }

        consumer(entities.map { videoEntity -> videoEntity.toVideoDetails() })
    }
}