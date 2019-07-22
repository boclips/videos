package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.SubjectId
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
            subjects = setOf("subject-1"),
            ageRangeMax = 10,
            ageRangeMin = 5
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
        assertThat(collection.subjects).containsExactly(SubjectId(value = "subject-1"))
        assertThat(collection.bookmarks).containsExactly(UserId(value = "user-1"))
    }
}