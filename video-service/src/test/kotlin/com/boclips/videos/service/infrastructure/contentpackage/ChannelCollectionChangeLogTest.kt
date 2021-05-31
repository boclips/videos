package com.boclips.videos.service.infrastructure.contentpackage

import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChannelCollectionChangeLogTest : AbstractSpringIntegrationTest() {

    var channelCollectionChangeLog = ChannelCollectionChangeLog()

    @Test
    fun unsetDeliveryFrequency() {
        val channel = Document(
            mapOf(
                "_id" to "channelId",
                "deliveryFrequency" to "123"
            )
        )
        val anotherChannel = Document(
            mapOf(
                "_id" to "anotherChannelId",
                "deliveryFrequency" to "456"
            )
        )

        mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .insertMany(listOf(channel, anotherChannel))

        channelCollectionChangeLog.unsetDeliveryFrequency(mongoClient)

        val updated = mongoClient.getDatabase(DATABASE_NAME).getCollection("channels")
            .find().toList()

        Assertions.assertThat(updated).filteredOn { it.containsKey("deliveryFrequency") }.isEmpty()
    }
}
