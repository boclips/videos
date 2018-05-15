package com.boclips.api.testsupport

import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.types.ObjectId
import org.flywaydb.core.Flyway
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var webClient: WebTestClient

    @Autowired
    lateinit var flyway: Flyway

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Value("classpath:/mongo/*.json")
    lateinit var collections: Array<Resource>

    @Before
    fun setUp() {
        cleanDB()
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
    }

    private fun cleanDB() {
        flyway.apply {
            clean()
            migrate()
        }
    }
}