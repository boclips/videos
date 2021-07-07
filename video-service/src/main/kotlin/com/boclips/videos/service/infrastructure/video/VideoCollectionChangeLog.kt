package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.changock.migration.api.annotations.ChangeLog
import io.changock.migration.api.annotations.ChangeSet
import io.changock.migration.api.annotations.NonLockGuarded
import mu.KLogging
import java.util.Arrays.equals

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

    @ChangeSet(order = "002", id = "2021-07-06T13:00", author = "mjanik & mfarleyrose")
    fun addUpdatedAt(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updatedByIngestDate = mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
            .aggregate(
                listOf(
                    Aggregates.match(Filters.eq("updatedAt", null)),
                    Aggregates.replaceWith(Field("updatedAt", "blabla"))
                )
            )

        logger.info { "addUpdatedAt results: $updatedByIngestDate" }
    }
}
