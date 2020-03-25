package com.boclips.videos.service.config.application

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.discipline.DisciplineRepository
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.infrastructure.ApiAccessRuleService
import com.boclips.videos.service.infrastructure.ApiGetUserIdOverride
import com.boclips.videos.service.infrastructure.collection.CollectionSubjects
import com.boclips.videos.service.infrastructure.collection.MongoCollectionFilterAccessRuleAdapter
import com.boclips.videos.service.infrastructure.collection.MongoCollectionRepository
import com.boclips.videos.service.infrastructure.discipline.MongoDisciplineRepository
import com.boclips.videos.service.infrastructure.subject.MongoSubjectRepository
import com.boclips.videos.service.infrastructure.tag.MongoTagRepository
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import io.opentracing.Tracer
import io.opentracing.contrib.mongo.common.TracingCommandListener
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
class InfrastructureContext(
    val mongoProperties: MongoProperties,
    val tracer: Tracer
) {
    @Bean
    fun apiAccessRuleService(userServiceClient: UserServiceClient): ApiAccessRuleService {
        return ApiAccessRuleService(userServiceClient)
    }

    @Bean
    fun getUserIdOverride(userServiceClient: UserServiceClient): GetUserIdOverride {
        return ApiGetUserIdOverride(userServiceClient)
    }

    @Bean
    fun taskExecutor(): TaskExecutor {
        return ConcurrentTaskExecutor(Executors.newFixedThreadPool(3))
    }

    @Bean
    fun mongoClient(): MongoClient {
        return KMongo.createClient(
            MongoClientURI(
                mongoProperties.determineUri(),
                MongoClientOptions.builder().addCommandListener(TracingCommandListener.Builder(tracer).build())
            )
        )
    }

    @Bean
    fun mongoCollectionFilterContractAdapter() = MongoCollectionFilterAccessRuleAdapter()

    @Bean
    fun collectionSubjects(mongoSubjectRepository: MongoSubjectRepository): CollectionSubjects {
        return CollectionSubjects(mongoSubjectRepository)
    }

    @Bean
    fun mongoCollectionRepository(
        mongoClient: MongoClient,
        collectionSubjects: CollectionSubjects,
        batchProcessingConfig: BatchProcessingConfig
    ): MongoCollectionRepository {
        return MongoCollectionRepository(
            mongoClient = mongoClient,
            batchProcessingConfig = batchProcessingConfig,
            collectionSubjects = collectionSubjects
        )
    }

    @Bean
    fun mongoSubjectRepository(mongoClient: MongoClient): MongoSubjectRepository {
        return MongoSubjectRepository(mongoClient)
    }

    @Bean
    fun mongoTagRepository(mongoClient: MongoClient): TagRepository {
        return MongoTagRepository(mongoClient)
    }

    @Bean
    fun mongoDisciplineRepository(
        mongoClient: MongoClient,
        mongoSubjectRepository: MongoSubjectRepository
    ): DisciplineRepository {
        return MongoDisciplineRepository(mongoClient, mongoSubjectRepository)
    }
}
