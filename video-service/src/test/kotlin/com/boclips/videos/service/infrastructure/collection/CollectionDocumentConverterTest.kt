package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.Instant

class CollectionDocumentConverterTest {
    @Test
    fun `converts a collection document to collection`() {
        val timeNow = Instant.now()
        val originalAsset = CollectionDocument(
            id = ObjectId(),
            owner = "Hans",
            viewerIds = listOf("Fritz"),
            title = "A truly amazing collection",
            videos = emptyList(),
            updatedAt = timeNow,
            visibility = CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = false,
            bookmarks = setOf("user-1"),
            subjects = setOf(TestFactories.createSubjectDocument(name = "subject-1")),
            ageRangeMax = 10,
            ageRangeMin = 5,
            description = "Good description",
            attachments = setOf(
                AttachmentDocument(
                    id = ObjectId(),
                    description = "description",
                    type = "LESSON_PLAN",
                    linkToResource = "https://example.com/download"
                )
            )
        )

        val collection = CollectionDocumentConverter.toCollection(originalAsset)!!

        assertThat(collection.id).isNotNull
        assertThat(collection.owner.value).isEqualTo("Hans")
        assertThat(collection.viewerIds).containsExactly(UserId(value = "Fritz"))
        assertThat(collection.title).isEqualTo("A truly amazing collection")
        assertThat(collection.updatedAt).isEqualTo(timeNow)
        assertThat(collection.isPublic).isEqualTo(false)
        assertThat(collection.ageRange.min()).isEqualTo(5)
        assertThat(collection.ageRange.max()).isEqualTo(10)
        assertThat(collection.bookmarks).containsExactly(UserId(value = "user-1"))
        assertThat(collection.description).isEqualTo("Good description")
        assertThat(collection.subjects.first().name).isEqualTo("subject-1")
        assertThat(collection.subjects.first().id.value).isNotBlank()
        assertThat(collection.attachments).hasSize(1);
        assertThat(collection.attachments.first().attachmentId).isNotNull
        assertThat(collection.attachments.first().description).isEqualTo("description")
        assertThat(collection.attachments.first().type).isEqualTo(AttachmentType.LESSON_PLAN)
        assertThat(collection.attachments.first().linkToResource).isEqualTo("https://example.com/download")
    }
}
