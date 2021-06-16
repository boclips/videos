package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.eventbus.domain.contentpartner.Channel
import com.boclips.eventbus.domain.contentpartner.ChannelId
import com.boclips.eventbus.domain.contentpartner.ChannelIngestDetails
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.IngestType
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateChannelIndexTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateChannel: UpdateChannel

    @Test
    fun `reindex is triggered when channels are updated`() {
        val channel = saveChannel(name = "hello", ingest = IngestDetailsResource.custom())
        updateChannel(channelId = channel.id.value, upsertRequest = ChannelRequest(ingest = IngestDetailsResource.manual()))
        fakeEventBus.publish(
            ContentPartnerUpdated.builder()
                .contentPartner(
                    Channel.builder()
                        .id(ChannelId(channel.id.value))
                        .name("test-888")
                        .ingest(ChannelIngestDetails.builder().type("MANUAL").build())
                        .build()
                )
                .build()
        )
        val searchRequest =
            PaginatedIndexSearchRequest(
                query = ChannelQuery(ingestTypes = listOf(IngestType.MANUAL)),
                startIndex = 0,
                windowSize = 30
            )

        val results = channelIndex.search(searchRequest)

        assertThat(results.counts.totalHits).isEqualTo(1)
    }
}
