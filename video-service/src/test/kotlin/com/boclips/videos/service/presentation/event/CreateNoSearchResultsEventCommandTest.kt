package com.boclips.videos.service.presentation.event

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CreateNoSearchResultsEventCommandTest {
    val noSearchResultsEvent = CreateNoSearchResultsEventCommand(
        name = "Heiri",
        email = "heiri@schwizerland.ch",
        query = "is heidi Swiss",
        description = "I need to know"
    )

    @Test
    fun `validates query`() {
        Assertions.assertThatThrownBy { noSearchResultsEvent.copy(query = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { noSearchResultsEvent.copy(query = "").isValidOrThrows() }
    }

    @Test
    fun `validates email`() {
        Assertions.assertThatThrownBy { noSearchResultsEvent.copy(email = null).isValidOrThrows() }
        Assertions.assertThatThrownBy { noSearchResultsEvent.copy(email = "").isValidOrThrows() }
    }
}