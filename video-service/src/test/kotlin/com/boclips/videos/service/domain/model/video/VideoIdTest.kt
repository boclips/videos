package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class VideoIdTest {
    @Test
    fun `valid video ids consist of hex strings`() {
        assertDoesNotThrow { TestFactories.aValidId() }
        assertDoesNotThrow { VideoId(value = "5c9e5e38aa25da28896c9956\uFEFF") }
    }

    @Test
    fun `cannot create video ids which are not valid hex strings`() {
        assertThatThrownBy { VideoId(value = "not-an-id") }.isInstanceOf(IllegalVideoIdentifierException::class.java)
        assertThatThrownBy { VideoId(value = "123123412") }.isInstanceOf(IllegalVideoIdentifierException::class.java)
        assertThatThrownBy { VideoId(value = ObjectId().toHexString().substring(0, 23)) }.isInstanceOf(
            IllegalVideoIdentifierException::class.java
        )
    }
}
