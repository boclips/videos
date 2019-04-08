package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL

class CollectionsLinkBuilderTest {

    @Test
    fun `when public collections`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.publicCollections(
            projection = Projection.details,
            page = 0,
            size = 2
        )

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=details&public=true&page=0&size=2")
        assertThat(link.rel).isEqualTo("publicCollections")
    }

    @Test
    fun `when bookmarked collections`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.bookmarkedCollections(
            projection = Projection.details,
            page = 0,
            size = 2
        )

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=details&public=true&bookmarked=true&page=0&size=2")
        assertThat(link.rel).isEqualTo("bookmarkedCollections")
    }

    @Test
    fun `when collections of a user`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collectionsByUser(
            owner = "user1",
            projection = Projection.list,
            page = 0,
            size = 2
        )

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=list&owner=user1&page=0&size=2")
        assertThat(link.rel).isEqualTo("myCollections")
    }

    @Test
    fun `when collections`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collections()

        assertThat(link.href).isEqualTo("https://localhost/v1/collections")
        assertThat(link.rel).isEqualTo("collections")
    }

    @Test
    fun `when collection`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collection(id = "c123")

        assertThat(link.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("collection")
    }

    @Test
    fun `when edit and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.editCollection(TestFactories.createCollectionResource(id = "c123", isMine = true))

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("edit")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `when remove and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.removeCollection(TestFactories.createCollectionResource(id = "c123", isMine = true))

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("remove")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `when addVideo and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.addVideoToCollection(TestFactories.createCollectionResource(id = "c123", isMine = true))

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123/videos/{video_id}")
        assertThat(link.rel).isEqualTo("addVideo")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `when removeVideo and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.removeVideoFromCollection(TestFactories.createCollectionResource(id = "c123", isMine = true))

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123/videos/{video_id}")
        assertThat(link.rel).isEqualTo("removeVideo")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `when edit and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.editCollection(TestFactories.createCollectionResource(id = "c123"))

        assertThat(link).isNull()
    }

    @Test
    fun `when remove and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.removeCollection(TestFactories.createCollectionResource(id = "c123"))

        assertThat(link).isNull()
    }

    @Test
    fun `when addVideo and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.addVideoToCollection(TestFactories.createCollectionResource(id = "c123"))

        assertThat(link).isNull()
    }

    @Test
    fun `when removeVideo and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.removeVideoFromCollection(TestFactories.createCollectionResource(id = "c123"))

        assertThat(link).isNull()
    }

    @Test
    fun `when templated collection`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collection(id = null)

        assertThat(link.href).isEqualTo("https://localhost/v1/collections/{id}")
        assertThat(link.rel).isEqualTo("collection")
        assertThat(link.isTemplated).isTrue()
    }

    @Test
    fun `when self link`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=true&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.self()

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=list&public=true&page=0&size=2")
        assertThat(link.rel).isEqualTo("self")
    }

    @Test
    fun `when details`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.projections().details()

        val url = URL(link.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "details")
        assertThat(url).hasNoParameter("projection", "list")
        assertThat(url).hasParameter("public", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link.rel).isEqualTo("details")
    }

    @Test
    fun `when list`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=details&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.projections().list()

        val url = URL(link.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasNoParameter("projection", "details")
        assertThat(url).hasParameter("public", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link.rel).isEqualTo("list")
    }

    @Test
    fun `when next link and more pages`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(true))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasParameter("public", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "1")
        assertThat(url).hasNoParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link?.rel).isEqualTo("next")
    }

    @Test
    fun `when next link and no more pages`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(false))

        assertThat(link).isNull()
    }

    @Test
    fun `when next link and no page parameter sets second page`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(true))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasParameter("public", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "1")
        assertThat(url).hasParameter("size", "2")
        assertThat(link?.rel).isEqualTo("next")
    }

    @Test
    fun `bookmark when public and unbookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.bookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = false, isMine = false))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections/collection-id")
        assertThat(url).hasParameter("bookmarked", "true")
        assertThat(link?.rel).isEqualTo("bookmark")
    }

    @Test
    fun `bookmark when not public`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.bookmark(TestFactories.createCollectionResource(isPublic = false, isBookmarked = false, isMine = false))

        assertThat(link).isNull()
    }

    @Test
    fun `bookmark when mine`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.bookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = false, isMine = true))

        assertThat(link).isNull()
    }

    @Test
    fun `bookmark when public but already bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.bookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = true, isMine = false))

        assertThat(link).isNull()
    }

    @Test
    fun `unbookmark when public and bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.unbookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = true, isMine = false))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections/collection-id")
        assertThat(url).hasParameter("bookmarked", "false")
        assertThat(link?.rel).isEqualTo("unbookmark")
    }

    @Test
    fun `unbookmark when not public`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.unbookmark(TestFactories.createCollectionResource(isPublic = false, isBookmarked = true, isMine = false))

        assertThat(link).isNull()
    }

    @Test
    fun `unbookmark when mine`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.unbookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = true, isMine = true))

        assertThat(link).isNull()
    }

    @Test
    fun `unbookmark when public but not bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&public=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.unbookmark(TestFactories.createCollectionResource(isPublic = true, isBookmarked = false, isMine = false))

        assertThat(link).isNull()
    }
}