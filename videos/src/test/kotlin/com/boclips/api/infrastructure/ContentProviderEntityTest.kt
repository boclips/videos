package com.boclips.api.infrastructure

import com.boclips.api.infrastructure.contentprovider.ContentProviderEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ContentProviderEntityTest {
    @Test
    fun toContentProvider_transformsId() {
        val transformedContentProvider = ContentProviderEntity("the-id", "some-content-provider").toContentProvider()

        assertThat(transformedContentProvider.id).isEqualTo("the-id")
    }

    @Test
    fun toContentProvider_transformsName() {
        val transformedContentProvider = ContentProviderEntity("the-id", "some-content-provider").toContentProvider()

        assertThat(transformedContentProvider.name).isEqualTo("some-content-provider")
    }
}