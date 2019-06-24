package com.boclips.videos.service.application.contentPartner

import com.boclips.events.types.ContentPartnerExclusionFromSearchRequested
import com.boclips.events.types.ContentPartnerInclusionInSearchRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.application.contentPartner.RequestSearchUpdateByContentPartner.RequestType
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class RequestSearchUpdateByContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var requestSearchUpdateByContentPartner: RequestSearchUpdateByContentPartner

    @Test
    fun `publishes an inclusion-in-search event for a content partner`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.INCLUDE
        )

        val message = messageCollector.forChannel(topics.contentPartnerInclusionInSearchRequested()).poll()
        val event =
            objectMapper.readValue(message.payload.toString(), ContentPartnerInclusionInSearchRequested::class.java)
        assertThat(event.contentPartnerId).isEqualTo("deadb33f1225df4825e8b8f6")
    }

    @Test
    fun `throws if content partner is fictitious`() {
        assertThrows<ContentPartnerNotFoundException> {
            requestSearchUpdateByContentPartner.invoke(
                ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.INCLUDE
            )
        }
    }

    @Test
    fun `publishes an exclusion-from-search event for a content partner`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.EXCLUDE
        )

        val message = messageCollector.forChannel(topics.contentPartnerExclusionFromSearchRequested()).poll()
        val event =
            objectMapper.readValue(message.payload.toString(), ContentPartnerExclusionFromSearchRequested::class.java)
        assertThat(event.contentPartnerId).isEqualTo("deadb33f1225df4825e8b8f6")
    }
}