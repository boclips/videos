package com.boclips.videos.service.presentation.attachments

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

        val attachmentToResourceConverter = AttachmentToResourceConverter(attachmentsLinkBuilder)

        val resources = attachmentToResourceConverter.wrapAttachmentsInResource(setOf(attachment))

        val resource = resources.first()
        assertThat(resources).hasSize(1)

        assertThat(resource.content.id).isEqualTo("id")
        assertThat(resource.content.type).isEqualTo("LESSON_PLAN")
        assertThat(resource.content.description).isEqualTo("description")
        assertThat(resource.links).hasSize(1)
        assertThat(resource.links[0].rel).isEqualTo("download")
        assertThat(resource.links[0].href).isEqualTo("http://test.com")
    }
}
