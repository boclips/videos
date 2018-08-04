package com.boclips.videoanalyser.testsupport

import com.boclips.videoanalyser.infrastructure.boclips.UNIQUE_VIDEOS_IN_PLAYLIST
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.types.ObjectId
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional


@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class AbstractSpringIntegrationTest : AbstractWireMockTest() {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Value("classpath:/db/mongo/*.json")
    lateinit var collections: Array<Resource>

    @Before
    fun setUp() {
        cleanMongo()
    }

    private fun cleanMongo() {

        collections.forEach { collectionFile ->
            val collection = collectionFile.filename!!.removeSuffix(".json")
            mongoTemplate.dropCollection(collection)
            ObjectMapper().readValue(collectionFile.file.readText(), List::class.java)
                    .filterNotNull()
                    .map { it as MutableMap<String, Any> }
                    .forEach { document ->
                        document["_id"] = ObjectId(document["_id"] as String)
                        mongoTemplate.insert(document, collection)
                    }
        }

        mongoTemplate.indexOps("videodescriptors").ensureIndex(
                Index().named(UNIQUE_VIDEOS_IN_PLAYLIST)
                        .unique()
                        .on("connection.item", Sort.Direction.ASC)
                        .on("reference_id", Sort.Direction.ASC)
        )
    }
}