package com.boclips.videos.service.application.collection

import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.Projection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssembleCollectionFilterTest {
    @Test
    fun `assembles a public collections filter`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            public = true,
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.PUBLIC)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a private collections filter`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            owner = "user@site.com",
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.owner).isEqualTo("user@site.com")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.PRIVATE)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a bookmarked collections filter`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            bookmarked = true,
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.BOOKMARKED)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a bookmarked collections filter when both public and bookmarked collections are requested`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            bookmarked = true,
            public = true,
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.BOOKMARKED)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a public collections filter when both public and owner collections are requested`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            public = true,
            owner = "someone@somewhere.net",
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.PUBLIC)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a bookmarked collections filter when both bookmark and owner collections are requested`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            bookmarked = true,
            owner = "someone@somewhere.net",
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.BOOKMARKED)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles all collections filter`() {
        val filter = assembleCollectionFilter(
            query = "minute physics",
            subject = listOf("Physics"),
            page = 1,
            size = 30
        )

        assertThat(filter.query).isEqualTo("minute physics")
        assertThat(filter.subjects).containsOnly("Physics")
        assertThat(filter.visibility).isEqualTo(CollectionFilter.Visibility.ALL)
        assertThat(filter.pageNumber).isEqualTo(1)
        assertThat(filter.pageSize).isEqualTo(30)
    }

    @Test
    fun `uses sane defaults for paging information and projections`() {
        val filter = assembleCollectionFilter()

        assertThat(filter.pageNumber).isEqualTo(0)
        assertThat(filter.pageSize).isEqualTo(CollectionsController.COLLECTIONS_PAGE_SIZE)
        assertThat(filter.projection).isEqualTo(Projection.list)
    }

    @Test
    fun `allows to override the projection`() {
        val filter = assembleCollectionFilter(projection = Projection.details)
        assertThat(filter.projection).isEqualTo(Projection.details)
    }

    @Test
    fun `uses sane defaults for search related parameters`() {
        val filter = assembleCollectionFilter()

        assertThat(filter.query).isEmpty()
        assertThat(filter.subjects).isEmpty()
    }

    private val assembleCollectionFilter = AssembleCollectionFilter()
}
