package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionMetadataConverterTest {
    @Test
    fun `convert`() {
        val collection = TestFactories.createCollection(
            id = CollectionId(value = "test-id"),
            title = "Some Collection Title",
            owner = "12903012381",
            isPublic = false,
            bookmarks = setOf(UserId(value = "userId1"))
        )

        val collectionMetadata = CollectionMetadataConverter.convert(collection)

        assertThat(collectionMetadata.id).isEqualTo("test-id")
        assertThat(collectionMetadata.title).isEqualTo("Some Collection Title")
        assertThat(collectionMetadata.owner).isEqualTo("12903012381")
        assertThat(collectionMetadata.visibility).isEqualTo(CollectionVisibility.PRIVATE)
        assertThat(collectionMetadata.bookmarkedByUsers).containsExactly("userId1")
        assertThat(collectionMetadata.hasLessonPlans).isEqualTo(false)
    }

    @Test
    fun `convert with lesson plans`() {
        val collectionWithLessonPlan = TestFactories.createCollection(
            id = CollectionId(value = "test-id"),
            title = "Collection with lesson plan",
            owner = "12903012381",
            isPublic = false,
            bookmarks = setOf(UserId(value = "userId1")),
            attachments = setOf(AttachmentFactory.sampleWithLessonPlan())
        )

        val collectionMetadata = CollectionMetadataConverter.convert(collectionWithLessonPlan)

        assertThat(collectionMetadata.id).isEqualTo("test-id")
        assertThat(collectionMetadata.title).isEqualTo("Collection with lesson plan")
        assertThat(collectionMetadata.visibility).isEqualTo(CollectionVisibility.PRIVATE)
        assertThat(collectionMetadata.hasLessonPlans).isEqualTo(true)
    }
}
