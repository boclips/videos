package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.common.service.VideoRemapperService
import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class VideoRemapperMongoRepository(val mongoTemplate: MongoTemplate): VideoRemapperService {
    override fun remapBasketsPlaylistsAndCollections(duplicate: Duplicate) {
        remapPlaylists(duplicate)
        remapBaskets(duplicate)
    }

    private fun remapPlaylists(duplicate: Duplicate) {
        mongoTemplate.updateFirst(
                Query().addCriteria(Criteria.where("reference_id").`in`(duplicate.duplicates.map { it.id })),
                Update().set("reference_id", duplicate.originalVideo.id),
                "videodescriptors"
        )
    }

    private fun remapBaskets(duplicate: Duplicate) {
        mongoTemplate.updateFirst(
                Query().addCriteria(Criteria.where("asset_id").`in`(duplicate.duplicates.map { it.id })),
                Update().set("asset_id", duplicate.originalVideo.id),
                "orderlines"
        )
    }

}