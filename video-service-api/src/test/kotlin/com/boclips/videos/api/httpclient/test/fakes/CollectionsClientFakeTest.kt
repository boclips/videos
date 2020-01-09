package com.boclips.videos.api.httpclient.test.fakes

import feign.FeignException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CollectionsClientFakeTest {
    @Test
    fun `throws a NotFound exception when collection is not found`() {
        val fake = CollectionsClientFake()

        assertThatThrownBy { fake.getCollection("this does not exist") }
            .isInstanceOf(FeignException.NotFound::class.java)
    }
}