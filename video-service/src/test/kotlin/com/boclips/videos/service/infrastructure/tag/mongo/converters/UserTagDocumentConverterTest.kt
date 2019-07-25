package com.boclips.videos.service.infrastructure.tag.mongo.converters

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.infrastructure.tag.TagDocumentConverter
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTagDocumentConverterTest {

    @Test
    fun `convert a tag to document and back`() {
        val originalTag = Tag(
            id = TagId(aValidId()), label = "maths"
        )

        val document = TagDocumentConverter.toTagDocument(originalTag)
        val retrievedTag = TagDocumentConverter.toTag(document)

        assertThat(originalTag).isEqualTo(retrievedTag)
    }
}
