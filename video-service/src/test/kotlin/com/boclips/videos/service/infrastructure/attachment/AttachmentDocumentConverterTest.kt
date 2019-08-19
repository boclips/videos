package com.boclips.videos.service.infrastructure.attachment

import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class AttachmentDocumentConverterTest {
    @Test
    fun `it can convert from document to domain model`() {
        val id = ObjectId()
        val document = AttachmentDocument(
            id = id,
            type = "LESSON_PLAN",
            description = "description",
            linkToResource = "http://example.com/download"
        )

        val attachment = AttachmentDocumentConverter.convert(document)

        assertThat(attachment.attachmentId).isEqualTo(AttachmentId(id.toHexString()))
        assertThat(attachment.description).isEqualTo("description")
        assertThat(attachment.type).isEqualTo(AttachmentType.LESSON_PLAN)
        assertThat(attachment.linkToResource).isEqualTo("http://example.com/download")
    }
}
