package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subjects.SubjectId
import org.assertj.core.api.Assertions
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.Instant

class CollectionDocumentConverterTest {
    @Test
    fun `converts a collection document to collection`() {
        val originalAsset = CollectionDocument(
            id = ObjectId(),
            owner = "Hans",
            viewerIds = listOf("Fritz"),
            title = "A truly amazing collection",
            videos = emptyList(),
            updatedAt = Instant.now(),
            visibility = CollectionVisibilityDocument.PRIVATE,
            createdByBoclips = false,
            bookmarks = setOf("user-1"),
            subjects = setOf("subject-1"),
            ageRangeMax = 10,
            ageRangeMin = 5
        )

        val collection = CollectionDocumentConverter.toCollection(originalAsset)!!

        Assertions.assertThat(collection.id).isNotNull
        Assertions.assertThat(collection.owner.value).isEqualTo("Hans")
        Assertions.assertThat(collection.viewerIds).containsExactly(UserId(value = "Fritz"))
        Assertions.assertThat(collection.title).isEqualTo("A truly amazing collection")
        Assertions.assertThat(collection.updatedAt).isNotNull
        Assertions.assertThat(collection.isPublic).isEqualTo(false)
        Assertions.assertThat(collection.ageRange.min()).isEqualTo(5)
        Assertions.assertThat(collection.ageRange.max()).isEqualTo(10)
        Assertions.assertThat(collection.subjects).containsExactly(SubjectId(value = "subject-1"))
        Assertions.assertThat(collection.bookmarks).containsExactly(UserId(value = "user-1"))
    }
}