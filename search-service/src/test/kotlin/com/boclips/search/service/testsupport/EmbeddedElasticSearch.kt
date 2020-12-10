package com.boclips.search.service.testsupport

import com.boclips.search.service.infrastructure.ElasticSearchClient
import com.boclips.search.service.infrastructure.videos.legacy.KGenericContainer
import io.opentracing.util.GlobalTracer
import mu.KLogging
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.time.Duration

abstract class EmbeddedElasticSearchIntegrationTest {
    lateinit var esClient: RestHighLevelClient

    companion object : KLogging() {
        val CLIENT: ElasticSearchClient

        init {
            val httpPort = 9200
            val tcpPort = 9300
            var container: KGenericContainer? = null
            try {
                container = KGenericContainer("elasticsearch:${org.elasticsearch.Version.CURRENT}")
                    .withExposedPorts(httpPort, tcpPort)
                    .withEnv("discovery.type", "single-node")
                    .waitingFor(
                        HttpWaitStrategy()
                            .forPort(httpPort)
                            .forStatusCodeMatching { response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED }
                            .withStartupTimeout(Duration.ofMinutes(2))
                    )
                    .withLogConsumer { frame -> if (frame.bytes != null) logger.info { String(frame.bytes) } }

                container.start()
            } catch (exception: ExceptionInInitializerError) {
                println( "it seems ES is already running as process. No need to set up a docker with it" )
                logger.info { "it seems ES is already running as process. No need to set up a docker with it" }
            }

            CLIENT = ElasticSearchClient(
                scheme = "http",
                host = "localhost",
                port = container?.getMappedPort(httpPort) ?: httpPort,
                username = "",
                password = "",
                tracer = GlobalTracer.get()
            )
        }
    }

    @BeforeEach
    fun configureClient() {
        esClient = CLIENT.buildClient()
    }

    @AfterEach
    fun tearDown() {
        esClient.use { client ->
            client.indices().delete(DeleteIndexRequest("*"), RequestOptions.DEFAULT)
        }
    }
}
