package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.changock.migration.api.annotations.ChangeLog
import io.changock.migration.api.annotations.ChangeSet
import io.changock.migration.api.annotations.NonLockGuarded
import mu.KLogging

@ChangeLog(order = "002")
class VideoCollectionChangeLog {
    companion object : KLogging()

    @ChangeSet(order = "001", id = "2021-05-20T12:15", author = "mfarleyrose")
    fun unsetBlankCategory(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updateResult = mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
            .updateMany(
                Filters.eq("categories.channel.codeValue", ""),
                Updates.unset("categories")
            )

        logger.info { "unsetCategories results: $updateResult" }
    }

    @ChangeSet(order = "002", id = "2021-07-09T12:15", author = "mfarleyrose & mjanik")
    fun setUpdatedAt(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        // so we've run it by Robo because current mongo driver does not support $toDate but I'm leaving this in here
        // because this logic could be useful in the future
        // db.getCollection('videos').aggregate([
        //    {$match: {"updatedAt": null}},
        //    {$addFields: {"updatedAt": {$toString: {$toDate: "$_id"}}}},
        //    {$merge: "videos"}
        // ])
        //
        // the equivalent in mongo (if we wanted to copy from ingestedAt) would be:
        // mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
        //            .aggregate(
        //                listOf(
        //                    Aggregates.match(Filters.eq("updatedAt", null)),
        //                    Aggregates.addFields(Field("updatedAt", "\$ingestedAt")),
        //                    Aggregates.merge("videos")
        //                )
        //            ).toList() 
    }
}
