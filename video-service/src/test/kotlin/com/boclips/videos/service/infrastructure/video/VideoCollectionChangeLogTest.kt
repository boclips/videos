package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.mongodb.client.model.Filters
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.litote.kmongo.findOne

class VideoCollectionChangeLogTest : AbstractSpringIntegrationTest() {

    private val videoCollectionChangeLog = VideoCollectionChangeLog()

    @Test
    fun `should remove categories field when it contains blank category only`() {
        val videoToBeUpdated = Document(
            mapOf(
                "_id" to "video-id",
                "categories" to Document(
                    mapOf(
                        "channel" to Document(
                            mapOf(
                                "codeValue" to "",
                                "description" to "",
                                "ancestors" to Document()
                            )
                        ),
                    ),
                )
            )
        )
        val correctVideo = Document(
            mapOf(
                "_id" to "video-id2",
                "categories" to Document(
                    mapOf(
                        "channel" to Document(
                            mapOf(
                                "codeValue" to "ABC",
                                "description" to "Archery, Baking, Computing",
                                "ancestors" to Document()
                            )
                        ),
                    ),
                )
            )
        )

        getVideosCollection()
            .insertMany(listOf(videoToBeUpdated, correctVideo))

        videoCollectionChangeLog.unsetBlankCategory(mongoClient)

        val updated = getVideosCollection()
            .findOne(Filters.eq("_id", "video-id"))

        Assertions.assertThat(updated).isNotNull
        Assertions.assertThat(updated).doesNotContainKey("categories")

        val intact = getVideosCollection()
            .findOne(Filters.eq("_id", "video-id2"))

        Assertions.assertThat(intact).isEqualTo(correctVideo)
    }

    @Test
    fun `updates empty updatedAt field with ingestedAt value`() {
        val videoWithoutUpdatedAt = Document(
            mapOf(
                "_id" to ObjectId(),
                "ingestedAt" to "2015-09-17T09:02:29Z",
            )
        )
        val videoWithUpdatedAt = Document(
            mapOf(
                "_id" to ObjectId(),
                "ingestedAt" to "2015-09-17T09:02:29Z",
                "updatedAt" to "2020-09-17T09:02:29Z",
            )
        )
        val videoWithoutAnyDates = Document(
            mapOf(
                "_id" to ObjectId("5c54a85cd8eafeecae0805c4")
            )
        )

        getVideosCollection().insertMany(listOf(videoWithUpdatedAt, videoWithoutAnyDates, videoWithoutUpdatedAt))

        videoCollectionChangeLog.addUpdatedAt(mongoClient)

        assertThat(getVideosCollection().find(Filters.exists("updatedAt")).toList()).hasSize(3)
        val shouldNotBeUpdated = getVideosCollection().findOne(Filters.eq("_id", videoWithUpdatedAt["_id"]!!))
        assertThat(shouldNotBeUpdated?.get("updatedAt")!!).isEqualTo(videoWithUpdatedAt["updatedAt"])
    }

    private fun getVideosCollection() = mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
}
