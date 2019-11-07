package com.boclips.videos.service.config.application

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.ApiAccessRuleService
import com.boclips.videos.service.infrastructure.collection.CollectionSubjects
import com.boclips.videos.service.infrastructure.collection.MongoCollectionFilterContractAdapter
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executors

@EnableRetry
@Configuration
class InfrastructureContext(val mongoProperties: MongoProperties) {
    @Bean
    fun apiAccessRuleService(userServiceClient: UserServiceClient): ApiAccessRuleService {
        return ApiAccessRuleService(userServiceClient)
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(Executors.newFixedThreadPool(3))
    }

    @Bean
    fun mongoClient(): MongoClient {
        return KMongo.createClient(MongoClientURI(mongoProperties.determineUri()))
    }

    @Bean
    fun mongoCollectionFilterContractAdapter() = MongoCollectionFilterContractAdapter()

    @Bean
    fun collectionSubjects(subjectRepository: SubjectRepository): CollectionSubjects {
        return CollectionSubjects(subjectRepository)
    }
}
