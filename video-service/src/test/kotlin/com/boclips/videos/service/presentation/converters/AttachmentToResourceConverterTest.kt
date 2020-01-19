package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttachmentToResourceConverterTest {
    @Test
    fun `it can wrap a single Attachment to a resource`() {
        val attachment = Attachment(
            attachmentId = AttachmentId("id"),
            description = "description",
            type = AttachmentType.LESSON_PLAN,
            linkToResource = "http://test.com"
        )

        val attachmentsLinkBuilder = AttachmentsLinkBuilder()

        val attachmentToResourceConverter =
            AttachmentToResourceConverter(
                attachmentsLinkBuilder
            )

        val resource = attachmentToResourceConverter.convert(attachment)

        assertThat(resource.id).isEqualTo("id")
        assertThat(resource.type).isEqualTo("LESSON_PLAN")
        assertThat(resource.description).isEqualTo("description")
        assertThat(resource._links).hasSize(1)
        assertThat(resource._links!!["download"]).isNotNull
        assertThat(resource._links!!["download"]?.href).isEqualTo("http://test.com")
    }
}
