package com.boclips.api

import com.fasterxml.jackson.databind.ObjectMapper
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

    @Value("classpath:/mongo/video-playlist.json")
    lateinit var videoPlaylist: Resource

    @Value("classpath:/mongo/orderlines.json")
    lateinit var orderlines: Resource

    @Value("classpath:/mongo/sources.json")
    lateinit var sources: Resource

    @Before
    fun setUp() {
        cleanDB()
        cleanMongo()
    }

    private fun cleanMongo() {
        mongoTemplate.dropCollection("videodescriptors")
        ObjectMapper().readValue(videoPlaylist.file.readText(), List::class.java).forEach {
            mongoTemplate.insert(it!!, "videodescriptors")
        }
        mongoTemplate.dropCollection("orderlines")
        ObjectMapper().readValue(orderlines.file.readText(), List::class.java).forEach {
            mongoTemplate.insert(it!!, "orderlines")
        }
        mongoTemplate.dropCollection("sources")
        ObjectMapper().readValue(sources.file.readText(), List::class.java).forEach {
            mongoTemplate.insert(it!!, "sources")
        }
    }

    private fun cleanDB() {
        flyway.apply {
            clean()
            migrate()
        }
    }
}