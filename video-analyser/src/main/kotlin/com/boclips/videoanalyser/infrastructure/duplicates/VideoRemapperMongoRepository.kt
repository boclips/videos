package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.domain.model.DuplicateVideo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class VideoRemapperMongoRepository(val mongoTemplate: MongoTemplate) : VideoRemapperService {
    override fun remapBasketsPlaylistsAndCollections(duplicateVideo: DuplicateVideo) {
        remapPlaylists(duplicateVideo)
        remapBaskets(duplicateVideo)
    }

    private fun remapPlaylists(duplicateVideo: DuplicateVideo) {
        mongoTemplate.updateFirst(
                Query().addCriteria(Criteria.where("reference_id").`in`(duplicateVideo.duplicates.map { it.id })),
                Update().set("reference_id", duplicateVideo.originalVideo.id),
                "videodescriptors"
        )
    }

    private fun remapBaskets(duplicateVideo: DuplicateVideo) {
        mongoTemplate.updateFirst(
                Query().addCriteria(Criteria.where("asset_id").`in`(duplicateVideo.duplicates.map { it.id })),
                Update().set("asset_id", duplicateVideo.originalVideo.id),
                "orderlines"
        )
    }

}