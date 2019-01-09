package com.boclips.search.service.infrastructure.solr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

class SolrIntegrationTest {

    companion object {

        var solrServer = KGenericContainer("boclipsconcourse/solr:0.33.0")
                .withExposedPorts(8983)
                .withLogConsumer { frame -> if(frame.bytes != null) print("SOLR: " + String(frame.bytes)) }

        @BeforeAll
        @JvmStatic
        fun startSolr() {
            solrServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            solrServer.stop()
        }
    }

    @Test
    fun `solr is running`() {
        assertThat(true).isTrue()
    }
}
