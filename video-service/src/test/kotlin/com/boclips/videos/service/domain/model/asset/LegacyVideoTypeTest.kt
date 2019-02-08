package com.boclips.videos.service.domain.model.asset

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class LegacyVideoTypeTest {

    @Test
    fun `parses valid type id to the correct instance`() {
        assertThat(LegacyVideoType.fromId(1)).isEqualTo(LegacyVideoType.NEWS)
    }

    @Test
    fun `invalid type id throws an exception`() {
        assertThatThrownBy { LegacyVideoType.fromId(100) }.hasMessage("The type id 100 is invalid")
    }
}