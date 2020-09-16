package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.*
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchChannelIdContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a matching video by channel id`(
        readService: IndexReader<VideoMetadata, VideoQuery>,
        writeService: IndexWriter<VideoMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentPartnerId = "karate"),
                SearchableVideoMetadataFactory.create(id = "2", contentPartnerId = "bjj"),
                SearchableVideoMetadataFactory.create(id = "3", contentPartnerId = "bjj"),
                SearchableVideoMetadataFactory.create(id = "4", contentPartnerId = "judo")
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = VideoQuery(
                    accessRuleQuery = AccessRuleQuery(deniedVideoIds = setOf("karate")),
                    userQuery = UserQuery(channelIds = setOf("bjj", "judo"))
            ))
        )

        assertThat(result.elements).containsExactlyInAnyOrder("2", "3", "4")
        assertThat(result.counts.totalHits).isEqualTo(3)
    }
}
