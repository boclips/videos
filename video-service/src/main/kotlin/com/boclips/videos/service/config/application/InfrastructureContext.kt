package com.boclips.videos.service.config.application

import com.boclips.videos.service.infrastructure.collection.MongoCollectionFilterContractAdapter
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors

@Configuration
class InfrastructureContext(val mongoProperties: MongoProperties) {
    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(Executors.newFixedThreadPool(3))
    }

    @Bean
    fun mongoClient(): MongoClient {
        println("Creating bean mongo client")
        return KMongo.createClient(MongoClientURI(mongoProperties.determineUri()))
    }

    @Bean
    fun mongoCollectionFilterContractAdapter() = MongoCollectionFilterContractAdapter()
}
