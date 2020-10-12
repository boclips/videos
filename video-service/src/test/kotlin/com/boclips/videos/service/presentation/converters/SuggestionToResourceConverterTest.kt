package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.testsupport.ChannelFactory
import com.boclips.videos.service.testsupport.SuggestionFactory
import com.boclips.videos.service.testsupport.TestFactories.createSubject
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionToResourceConverterTest {
    @Test
    fun `convert suggestions`() {
        val subjectOne = createSubject(name = "Biology")
        val subjectTwo = createSubject(name = "Biological Sciences")
        val channels = listOf(
            ChannelFactory.create(id = "123", name = "Ted"),
            ChannelFactory.create(id = "234", name = "Crash Course Biology")
        )
        val suggestions = SuggestionFactory.create(
            channels = channels,
            subjects = listOf(subjectOne, subjectTwo)
        )

        val resource = SuggestionToResourceConverter(mock()).convert(query = "ted", suggestions = suggestions)

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
