package com.boclips.search.service.infrastructure.legacy

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.VideoQuery
import com.boclips.search.service.domain.legacy.SolrDocumentNotFound
import com.boclips.search.service.domain.legacy.SolrException
import com.boclips.search.service.testsupport.LegacyVideoMetadataFactory
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.common.params.CoreAdminParams
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

class SolrSearchServiceIntegrationTest {

    companion object {

        val containerPort = 8983

        var solrServer = KGenericContainer("boclipsconcourse/solr:0.33.0")
            .withExposedPorts(containerPort)
            .withLogConsumer { frame -> if (frame.bytes != null) print("SOLR: " + String(frame.bytes)) }

        fun getPort(): Int {
            return solrServer.getMappedPort(containerPort)
        }
    }

    @BeforeEach
    fun startSolr() {
        solrServer.start()

        while (!coreReady()) {
            Thread.sleep(10)
        }

        solrSearchService = SolrSearchService("localhost", getPort())
    }

    private fun coreReady(): Boolean {
        val client = HttpSolrClient.Builder("http://localhost:${getPort()}/solr").build()!!
        val request = CoreAdminRequest()
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS)
        val result = request.process(client)
        return result.coreStatus.size() > 0
    }

    @AfterEach
    fun tearDown() {
        solrServer.stop()
    }

    lateinit var solrSearchService: SolrSearchService

    @Test
    fun `does not support searching by phrase`() {
        assertThrows<UnsupportedOperationException> { solrSearchService.search(PaginatedSearchRequest(
            VideoQuery(
                phrase = "phrase"
            )
        )) }
    }

    @Test
    fun `upsert a video`() {
        solrSearchService.upsert(
            sequenceOf(
                LegacyVideoMetadataFactory.create(id = "1"),
                LegacyVideoMetadataFactory.create(id = "2"),
                LegacyVideoMetadataFactory.create(id = "3")
            )
        )

        val results = solrSearchService.search(PaginatedSearchRequest(
            VideoQuery(
                ids = listOf("1", "2", "5")
            )
        ))
        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `remove a video from the index`() {
        solrSearchService.upsert(sequenceOf(LegacyVideoMetadataFactory.create(id = "10")))

        solrSearchService.removeFromSearch(videoId = "10")

        val results = solrSearchService.search(PaginatedSearchRequest(
            VideoQuery(
                ids = listOf("10")
            )
        ))
        assertThat(results).isEmpty()
    }

    @Test
    fun `throws when video does not exist for deletion`() {
        assertThatThrownBy { solrSearchService.removeFromSearch(videoId = "10") }
            .isInstanceOf(SolrDocumentNotFound::class.java)
            .hasMessage("Video 10 not found")
    }

    @Test
    fun `throws when there is an error communicating with solr`() {
        solrServer.stop()

        assertThatThrownBy { solrSearchService.removeFromSearch(videoId = "10") }
            .isInstanceOf(SolrException::class.java)
    }
}
