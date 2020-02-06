package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.api.request.collection.CollectionSortKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionSearchQueryTest {

    @Test
    fun `no sort by default`() {
        val query = CollectionSearchQuery(
            text = "sometin",
            subjectIds = listOf("subject"),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = null,
            hasLessonPlans = null
        )

        assertThat(query.toSearchQuery().sort).isNull()
    }

    @Test
    fun `when not searching by text sorts by attachments`() {
        val query = CollectionSearchQuery(
            text = null,
            subjectIds = listOf("subject"),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = null,
            hasLessonPlans = null
        )

        assertThat(query.toSearchQuery().sort).isEqualTo(
            Sort.ByField(
                CollectionMetadata::hasAttachments,
                SortOrder.DESC
            )
        )
    }

    @Test
    fun `when not filtering sorts by attachments`() {
        val query = CollectionSearchQuery(
            text = null,
            subjectIds = emptyList(),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = null,
            hasLessonPlans = null
        )

        assertThat(query.toSearchQuery().sort).isEqualTo(
            Sort.ByField(
                CollectionMetadata::hasAttachments,
                SortOrder.DESC
            )
        )
    }

    @Test
    fun `can sort by title when searching with text`() {
        val query = CollectionSearchQuery(
            text = "a dog",
            subjectIds = emptyList(),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = null,
            hasLessonPlans = null,
            sort = CollectionSortKey.TITLE
        )

        assertThat(query.toSearchQuery().sort).isEqualTo(
            Sort.ByField(
                CollectionMetadata::title,
                SortOrder.ASC
            )
        )
    }

    @Test
    fun `can sort by title when searching without text`() {
        val query = CollectionSearchQuery(
            text = null,
            subjectIds = emptyList(),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = null,
            hasLessonPlans = null,
            sort = CollectionSortKey.TITLE
        )

        assertThat(query.toSearchQuery().sort).isEqualTo(
            Sort.ByField(
                CollectionMetadata::title,
                SortOrder.ASC
            )
        )
    }

    @Test
    fun `sets permitted ids`() {
        val query = CollectionSearchQuery(
            text = "sometin",
            subjectIds = listOf("subject"),
            visibilityForOwners = emptySet(),
            pageIndex = 0,
            pageSize = 0,
            permittedCollections = listOf(CollectionId("some-collection-id")),
            hasLessonPlans = null
        )

        assertThat(query.toSearchQuery().permittedIds).containsExactly("some-collection-id")
    }
}
