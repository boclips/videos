package com.boclips.videos.service.application.tag

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.application.exceptions.TagExistsException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateTagTest : AbstractSpringIntegrationTest() {
    @Test
    fun `throws when missing label`() {
        val createRequest = TestFactories.createTagRequest(
            label = null
        )

        assertThrows<NonNullableFieldCreateRequestException> { createTag(createRequest) }
    }

    @Test
    fun `throws when tag already exists`() {
        val createRequest = TestFactories.createTagRequest(label = "Music")

        createTag(createRequest)

        assertThrows<TagExistsException> { createTag(createRequest) }
    }
}
