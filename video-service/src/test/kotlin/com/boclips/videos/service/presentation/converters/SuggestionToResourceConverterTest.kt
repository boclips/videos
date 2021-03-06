package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import com.boclips.videos.service.testsupport.ChannelFactory
import com.boclips.videos.service.testsupport.SuggestionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionToResourceConverterTest {
    @Test
    fun `convert suggestions`() {
        val subjectOne = SubjectSuggestion(name = "nice subject", id = SubjectId("1"))
        val subjectTwo = SubjectSuggestion(name = "nicer subject", id = SubjectId("2"))

        val channels = listOf(
            ChannelFactory.createSuggestion(id = "123", name = "Ted"),
            ChannelFactory.createSuggestion(id = "234", name = "Crash Course Biology")
        )

        val suggestions = SuggestionFactory.create(
            channels = channels,
            subjects = listOf(subjectOne, subjectTwo)
        )

        val resource = SuggestionToResourceConverter().convert(query = "ted", suggestions = suggestions)

        assertThat(resource.channels[0].id).isEqualTo("123")
        assertThat(resource.channels[1].id).isEqualTo("234")

        assertThat(resource.channels[0].name).isEqualTo("Ted")
        assertThat(resource.channels[1].name).isEqualTo("Crash Course Biology")

        assertThat(resource.subjects[0].name).isEqualTo(subjectOne.name)
        assertThat(resource.subjects[1].name).isEqualTo(subjectTwo.name)

        assertThat(resource.subjects[0].id).isEqualTo(subjectOne.id.value)
        assertThat(resource.subjects[1].id).isEqualTo(subjectTwo.id.value)
    }
}
