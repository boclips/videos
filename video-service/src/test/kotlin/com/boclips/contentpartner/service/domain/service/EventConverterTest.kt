package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.contentpartner.CustomIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.YoutubeScrapeIngest
import com.boclips.eventbus.domain.contract.ContractId
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EventConverterTest {

    private val converter: EventConverter = EventConverter()

    @Test
    fun `convert manual ingest details`() {
        val payload = converter.toIngestDetailsPayload(ManualIngest)

        assertThat(payload.type).isEqualTo(IngestType.MANUAL.name)
        assertThat(payload.urls).isNull()
    }

    @Test
    fun `convert custom ingest details`() {
        val payload = converter.toIngestDetailsPayload(CustomIngest)

        assertThat(payload.type).isEqualTo(IngestType.CUSTOM.name)
        assertThat(payload.urls).isNull()
    }

    @Test
    fun `convert mrss ingest details`() {
        val payload = converter.toIngestDetailsPayload(
            MrssFeedIngest(
                listOf(
                    "http://feed1.me",
                    "http://feed2.me"
                )
            )
        )

        assertThat(payload.type).isEqualTo(IngestType.MRSS.name)
        assertThat(payload.urls).containsExactly("http://feed1.me", "http://feed2.me")
    }

    @Test
    fun `convert youtube ingest details`() {
        val payload = converter.toIngestDetailsPayload(
            YoutubeScrapeIngest(
                listOf(
                    "http://yt1.channel",
                    "http://yt2.channel"
                )
            )
        )

        assertThat(payload.type).isEqualTo(IngestType.YOUTUBE.name)
        assertThat(payload.urls).containsExactly("http://yt1.channel", "http://yt2.channel")
    }

    @Test
    fun `convert contract`() {
        val originalContract = ContentPartnerContractFactory.sample(id = "blah", contentPartnerName = "BANANA")
        val convertedContract = converter.toContractPayload(originalContract)

        assertThat(convertedContract.contractId.value).isEqualTo("blah")
        assertThat(convertedContract.name).isEqualTo("BANANA")
    }
}
