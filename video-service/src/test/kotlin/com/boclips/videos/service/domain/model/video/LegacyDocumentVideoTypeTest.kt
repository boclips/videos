package com.boclips.videos.service.domain.model.video

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class LegacyDocumentVideoTypeTest {

    @Test
    fun `parses valid type id to the correct instance`() {
        assertThat(ContentType.fromId(1)).isEqualTo(ContentType.NEWS)
    }

    @Test
    fun `invalid type id throws an exception`() {
        assertThatThrownBy { ContentType.fromId(100) }.hasMessage("The type id 100 is invalid")
    }
}
