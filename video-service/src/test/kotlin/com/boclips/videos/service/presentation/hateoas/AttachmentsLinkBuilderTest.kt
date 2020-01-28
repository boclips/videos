package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Link

class AttachmentsLinkBuilderTest {
    @Test
    fun `it makes a download link for the attachment`() {
        val linkBuilder = AttachmentsLinkBuilder()

        val attachment = Attachment(
            attachmentId = AttachmentId("id"),
            description = "Description",
            type = AttachmentType.LESSON_PLAN,
            linkToResource = "http://example.com/download"
        )

        val link: Link = linkBuilder.download(attachment)

        assertThat(link.href).isEqualTo("http://example.com/download")
        assertThat(link.rel.value()).isEqualTo("download")
    }
}
