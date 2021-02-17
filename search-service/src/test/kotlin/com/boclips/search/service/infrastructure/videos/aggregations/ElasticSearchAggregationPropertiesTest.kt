package com.boclips.search.service.infrastructure.videos.aggregations

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    "elasticsearch.aggregation-limits.subjects=4",
    "elasticsearch.aggregation-limits.attachment-types=55",
    "elasticsearch.aggregation-limits.video-types=4",
    "elasticsearch.aggregation-limits.video-prices=4",
    "elasticsearch.aggregation-limits.channels=40",
])
@EnableConfigurationProperties(value = [ElasticSearchAggregationProperties::class])
class ElasticSearchAggregationPropertiesTest {

    @Autowired
    private var properties: ElasticSearchAggregationProperties? = null

    @Test
    fun `should parse properties for aggregation limits correctly`() {
        assertThat(properties!!.subjects).isEqualTo(4)
        assertThat(properties!!.attachmentTypes).isEqualTo(55)
        assertThat(properties!!.videoTypes).isEqualTo(4)
        assertThat(properties!!.videoPrices).isEqualTo(4)
        assertThat(properties!!.channels).isEqualTo(40)
    }
}
