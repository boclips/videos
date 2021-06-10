package com.boclips.videos.service.domain.service.suggestions

import com.boclips.videos.service.domain.model.suggestions.request.SuggestionsRequest
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class SuggestionsRetrievalServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var suggestionsRetrievalService: SuggestionsRetrievalService

    @Nested
    inner class Retrieving {
        @Test
        fun `retrieve channel and subjects by query returns channel from elastic search`() {

            val superChannel = saveChannel(name = "Super Channel")
            saveChannel(name = "Bad Channel")
            val superSubject = saveSubject(name = "super subjects")
            saveSubject(name = "bad subjects")

            val results = suggestionsRetrievalService.findSuggestions(
                SuggestionsRequest(
                    text = "Super"
                ),
                VideoAccess.Everything(emptySet())
            )

            assertThat(results.channels[0].id).isEqualTo(superChannel.id)
            assertThat(results.subjects[0].id).isEqualTo(superSubject.id)
        }
    }
}
