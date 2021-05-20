package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.mongodb.client.model.Filters
import org.assertj.core.api.Assertions
import org.bson.Document
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

        mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
            .insertMany(listOf(videoToBeUpdated, correctVideo))

        videoCollectionChangeLog.unsetBlankCategory(mongoClient)

        val updated = mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
            .findOne(Filters.eq("_id", "video-id"))

        Assertions.assertThat(updated).isNotNull
        Assertions.assertThat(updated).doesNotContainKey("categories")

        val intact = mongoClient.getDatabase(DATABASE_NAME).getCollection("videos")
            .findOne(Filters.eq("_id", "video-id2"))

        Assertions.assertThat(intact).isEqualTo(correctVideo)
    }
}
