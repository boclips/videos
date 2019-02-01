package com.boclips.videos.service.infrastructure.collection.mongo

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionDocumentConverterTest {
    @Test
    fun `convert to and from document`() {
        val collectionInput = CollectionDocument(
                id = TestFactories.aValidId(),
                owner = "some-user",
                title = "some-title",
                videos = listOf(TestFactories.aValidId())
        )

        val document = CollectionDocumentConverter().toDocument(collectionInput)
        val collectionOutput = CollectionDocumentConverter().fromDocument(document)

        assertThat(collectionInput).isEqualTo(collectionOutput)
    }
}
