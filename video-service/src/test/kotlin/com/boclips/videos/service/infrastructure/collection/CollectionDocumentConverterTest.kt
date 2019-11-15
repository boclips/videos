package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class CollectionDocumentConverterTest {

    val collectionDocument = CollectionDocument(
        id = ObjectId(),
        owner = "Hans",
        title = "A truly amazing collection",
        videos = emptyList(),
        createdAt = ZonedDateTime.of(2019, 3, 2, 1, 2, 3, 0, ZoneOffset.UTC).toInstant(),
        updatedAt = ZonedDateTime.of(2019, 4, 3, 2, 1, 2, 0, ZoneOffset.UTC).toInstant(),
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

    @Test
    fun `converts a collection document to collection`() {
        val collection = CollectionDocumentConverter.toCollection(collectionDocument)!!

        assertThat(collection.id).isNotNull
        assertThat(collection.owner.value).isEqualTo("Hans")
        assertThat(collection.title).isEqualTo("A truly amazing collection")
        assertThat(collection.createdAt).isEqualTo("2019-03-02T01:02:03Z")
        assertThat(collection.updatedAt).isEqualTo("2019-04-03T02:01:02Z")
        assertThat(collection.isPublic).isEqualTo(false)
        assertThat(collection.ageRange.min()).isEqualTo(5)
        assertThat(collection.ageRange.max()).isEqualTo(10)
        assertThat(collection.bookmarks).containsExactly(UserId(value = "user-1"))
        assertThat(collection.description).isEqualTo("Good description")
        assertThat(collection.subjects.first().name).isEqualTo("subject-1")
        assertThat(collection.subjects.first().id.value).isNotBlank()
        assertThat(collection.attachments).hasSize(1)
        assertThat(collection.attachments.first().attachmentId).isNotNull
        assertThat(collection.attachments.first().description).isEqualTo("description")
        assertThat(collection.attachments.first().type).isEqualTo(AttachmentType.LESSON_PLAN)
        assertThat(collection.attachments.first().linkToResource).isEqualTo("https://example.com/download")
    }

    @Test
    fun `derives creation time from id when timestamp is missing`() {
        val collection = CollectionDocumentConverter.toCollection(collectionDocument.copy(
            id = ObjectId("5dcd28400000000000000000"),
            createdAt = null
        ))!!

        assertThat(collection.createdAt).isEqualTo("2019-11-14T10:11:12Z")
    }
}
