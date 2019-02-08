package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class AssetIdTest {
    @Test
    fun `valid asset ids consist of hex strings`() {
        assertDoesNotThrow { TestFactories.aValidId() }
    }

    @Test
    fun `cannot create asset ids which are not valid hex strings`() {
        assertThatThrownBy { AssetId(value = "not-an-id") }.isInstanceOf(IllegalVideoIdentifier::class.java)
        assertThatThrownBy { AssetId(value = "123123412") }.isInstanceOf(IllegalVideoIdentifier::class.java)
        assertThatThrownBy { AssetId(value = ObjectId().toHexString().substring(0, 23)) }.isInstanceOf(
            IllegalVideoIdentifier::class.java
        )
    }
}