package com.boclips.videos.service.domain.service.suggestions

import com.boclips.videos.service.domain.model.suggestions.request.ChannelsSuggestionsRequest
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class NewSuggestionsRetrievalServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var newSuggestionsRetrievalService: NewSuggestionsRetrievalService

    @Nested
    inner class Retrieving {
        @Test
        fun `retrieve channel by query returns channel from elastic search`() {

            val superChannel = saveChannel(name = "Super Channel")
            saveChannel(name = "Bad Channel")

            val results = newSuggestionsRetrievalService.findSuggestions(
                ChannelsSuggestionsRequest(
                    text = "Super"
                ),
                VideoAccess.Everything
            )

            assertThat(results.channels[0].id).isEqualTo(superChannel.id)
        }
    }
}
