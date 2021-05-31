package com.boclips.videos.service.infrastructure.contentpackage

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.changock.migration.api.annotations.ChangeLog
import io.changock.migration.api.annotations.ChangeSet
import io.changock.migration.api.annotations.NonLockGuarded
import mu.KLogging

@ChangeLog(order = "001")
class ChannelCollectionChangeLog {
    companion object : KLogging()

    @ChangeSet(order = "001", id = "1", author = "mjanik")
    fun unsetCurriculumAligned(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updateResult = mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .updateMany(
                Filters.exists("curriculumAligned"),
                Updates.unset("curriculumAligned")
            )

        logger.info { "unsetCurriculumAligned results: $updateResult" }
    }

    @ChangeSet(order = "002", id = "2", author = "mjanik")
    fun unsetEducationalResources(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updateResult = mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .updateMany(
                Filters.exists("educationalResources"),
                Updates.unset("educationalResources")
            )
        logger.info { "unsetEducationalResources results: $updateResult" }
    }

    @ChangeSet(order = "003", id = "3", author = "mjanik")
    fun unsetIsTranscriptProvided(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updateResult = mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .updateMany(
                Filters.exists("isTranscriptProvided"),
                Updates.unset("isTranscriptProvided")
            )
        logger.info { "unsetIsTranscriptProvided results: $updateResult" }
    }

    @ChangeSet(order = "003", id = "2021-05-31T10:10", author = "mjanik")
    fun unsetDeliveryFrequency(
        @NonLockGuarded mongoClient: MongoClient,
    ) {
        val updateResult = mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .updateMany(
                Filters.exists("deliveryFrequency"),
                Updates.unset("deliveryFrequency")
            )
        logger.info { "unsetDeliveryFrequency results: $updateResult" }
    }
}
