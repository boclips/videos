package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.infrastructure.duplicates.VideoRemapperService
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

const val UNIQUE_VIDEOS_IN_PLAYLIST = "connection.item_1_reference_id_1"

@Component
class VideoRemapperMongoRepository(val mongoTemplate: MongoTemplate) : VideoRemapperService {
    override fun disableIndexesBeforeRemapping() {
        mongoTemplate.indexOps("videodescriptors").dropIndex(UNIQUE_VIDEOS_IN_PLAYLIST)
    }

    override fun enableIndexesAfterRemapping() {
        mongoTemplate.indexOps("videodescriptors").ensureIndex(
                Index().named(UNIQUE_VIDEOS_IN_PLAYLIST)
                        .unique()
                        .on("connection.item", Sort.Direction.ASC)
                        .on("reference_id", Sort.Direction.ASC)
        )
    }

    override fun remapBasketsPlaylistsAndCollections(duplicate: DuplicateVideo) {
        remapPlaylists(duplicate)
        remapBaskets(duplicate)
    }

    private fun remapPlaylists(duplicate: DuplicateVideo) {
        mongoTemplate.updateMulti(
                Query().addCriteria(Criteria.where("reference_id").`in`(duplicate.duplicates.map { it.id })),
                Update().set("reference_id", duplicate.originalVideo.id),
                "videodescriptors"
        )
        removeDuplicatesInPlaylists()
    }

    private fun removeDuplicatesInPlaylists() {
        val output = mongoTemplate.aggregate(Aggregation.newAggregation(
                group("connection.item", "reference_id")
                        .addToSet("_id").`as`("dups")
                        .count().`as`("count"),
                match(where("count").gt(1))
        ), "videodescriptors", Map::class.java)

        output.mappedResults.flatMap {
            (it["dups"] as List<ObjectId>).drop(1)
        }.forEach { mongoTemplate.remove(Query.query(where("_id").`is`(it)), "videodescriptors") }
    }

    private fun remapBaskets(duplicate: DuplicateVideo) {
        mongoTemplate.updateMulti(
                Query().addCriteria(Criteria.where("asset_id").`in`(duplicate.duplicates.map { it.id })),
                Update().set("asset_id", duplicate.originalVideo.id),
                "orderlines"
        )
    }

}