package com.boclips.search.service.infrastructure.legacy

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.testsupport.LegacyVideoMetadataFactory
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.common.params.CoreAdminParams
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

class SolrSearchServiceIntegrationTest {

    companion object {

        val containerPort = 8983

        var solrServer = KGenericContainer("boclipsconcourse/solr:0.33.0")
                .withExposedPorts(containerPort)
                .withLogConsumer { frame -> if (frame.bytes != null) print("SOLR: " + String(frame.bytes)) }

        @BeforeAll
        @JvmStatic
        fun startSolr() {
            solrServer.start()

            while (!coreReady()) {
                Thread.sleep(10)
            }
        }

        private fun coreReady(): Boolean {
            val client = HttpSolrClient("http://localhost:${getPort()}/solr")
            val request = CoreAdminRequest()
            request.setAction(CoreAdminParams.CoreAdminAction.STATUS)
            val result = request.process(client)
            return result.coreStatus.size() > 0
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            solrServer.stop()
        }

        fun getPort(): Int {
            return solrServer.getMappedPort(containerPort)
        }
    }


    lateinit var solrSearchService: SolrSearchService

    @BeforeEach
    fun setUp() {
        solrSearchService = SolrSearchService("localhost", getPort())
    }

    @Test
    fun `does not support searching by phrase`() {
        assertThrows<UnsupportedOperationException> { solrSearchService.search(PaginatedSearchRequest(Query(phrase = "phrase"))) }
    }

    @Test
    fun `upsert a video`() {
        solrSearchService.upsert(sequenceOf(
                LegacyVideoMetadataFactory.create(id = "1"),
                LegacyVideoMetadataFactory.create(id = "2"),
                LegacyVideoMetadataFactory.create(id = "3")
        ))

        val results = solrSearchService.search(PaginatedSearchRequest(Query(ids = listOf("1", "2", "5"))))
        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }
}
