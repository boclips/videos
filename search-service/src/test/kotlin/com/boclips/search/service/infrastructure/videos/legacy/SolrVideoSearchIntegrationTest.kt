package com.boclips.search.service.infrastructure.videos.legacy

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.legacy.LegacyDocumentNotFound
import com.boclips.search.service.domain.videos.model.VideoQuery
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

        solrVideoSearch = SolrVideoSearchService("localhost", getPort())
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

    lateinit var solrVideoSearch: SolrVideoSearchService

    @Test
    fun `does not support searching by phrase`() {
        assertThrows<UnsupportedOperationException> {
            solrVideoSearch.search(
                PaginatedSearchRequest(
                    VideoQuery(
                        phrase = "phrase"
                    )
                )
            )
        }
    }

    @Test
    fun `upsert a video`() {
        solrVideoSearch.upsert(
            sequenceOf(
                LegacyVideoMetadataFactory.create(id = "1"),
                LegacyVideoMetadataFactory.create(id = "2"),
                LegacyVideoMetadataFactory.create(id = "3")
            )
        )

        val results = solrVideoSearch.search(
            PaginatedSearchRequest(
                VideoQuery(
                    ids = listOf("1", "2", "5")
                )
            )
        )
        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `remove a video from the index`() {
        solrVideoSearch.upsert(sequenceOf(LegacyVideoMetadataFactory.create(id = "10")))

        solrVideoSearch.removeFromSearch(itemId = "10")

        val results = solrVideoSearch.search(
            PaginatedSearchRequest(
                VideoQuery(
                    ids = listOf("10")
                )
            )
        )
        assertThat(results).isEmpty()
    }

    @Test
    fun `bulk removes videos from the index`() {
        solrVideoSearch.upsert(
            sequenceOf(
                LegacyVideoMetadataFactory.create(id = "1"),
                LegacyVideoMetadataFactory.create(id = "2"),
                LegacyVideoMetadataFactory.create(id = "3")
            )
        )

        solrVideoSearch.bulkRemoveFromSearch(listOf("1", "2", "3"))

        val results = solrVideoSearch.search(
            PaginatedSearchRequest(
                VideoQuery(
                    ids = listOf("1", "2", "3")
                )
            )
        )
        assertThat(results).isEmpty()
    }

    @Test
    fun `throws when video does not exist for deletion`() {
        assertThatThrownBy { solrVideoSearch.removeFromSearch(itemId = "10") }
            .isInstanceOf(LegacyDocumentNotFound::class.java)
            .hasMessage("Video 10 not found in legacy index")
    }

    @Test
    fun `does not fail bulk removal if one videos does not exist`() {
        solrVideoSearch.upsert(
            sequenceOf(
                LegacyVideoMetadataFactory.create(id = "1")
            )
        )

        solrVideoSearch.bulkRemoveFromSearch(listOf("1", "2"))

        val results = solrVideoSearch.search(
            PaginatedSearchRequest(
                VideoQuery(
                    ids = listOf("1", "2")
                )
            )
        )
        assertThat(results).isEmpty()
    }

    @Test
    fun `throws when there is an error communicating with solr`() {
        solrServer.stop()

        assertThatThrownBy { solrVideoSearch.removeFromSearch(itemId = "10") }
            .isInstanceOf(SolrException::class.java)
    }
}
