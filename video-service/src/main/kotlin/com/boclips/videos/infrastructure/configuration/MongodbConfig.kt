package com.boclips.videos.infrastructure.configuration

import com.mongodb.async.client.MongoClientSettings
import com.mongodb.connection.ConnectionPoolSettings
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.stereotype.Component


@Component
class MongodbConfig : MongoClientSettingsBuilderCustomizer {
    override fun customize(clientSettingsBuilder: MongoClientSettings.Builder) {
        clientSettingsBuilder.connectionPoolSettings(ConnectionPoolSettings.builder()
                .maxSize(20)
                .build())
    }


}