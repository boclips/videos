package com.boclips.videos.service.domain.model.collection

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CollectionSearchQueryTest {

    @Test
    fun `no sort by default`() {
        val query = CollectionSearchQuery(text = "sometin", subjectIds = listOf("subject"), pageIndex = 0, pageSize = 0)

        assertThat(query.toSearchQuery().sort).isNull()
    }

    @Test
    fun `when not filtering by text sorts by attachments`() {
        val query = CollectionSearchQuery(text = null, subjectIds = listOf("subject"), pageIndex = 0, pageSize = 0)

        assertThat(query.toSearchQuery().sort).isEqualTo(Sort(CollectionMetadata::hasAttachments, SortOrder.DESC))
    }
}