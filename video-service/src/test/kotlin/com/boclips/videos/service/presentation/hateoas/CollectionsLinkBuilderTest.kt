package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.api.request.Projection
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL

class CollectionsLinkBuilderTest {

    @AfterEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `discover collections`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.discoverCollections(
            projection = Projection.details,
            page = 0,
            size = 2
        )

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=details&discoverable=true&page=0&size=2")
        assertThat(link.rel).isEqualTo("discoverCollections")
    }

    @Test
    fun `when searching collections`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.searchCollections()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/collections{?query,subject,discoverable,projection,page,size,age_range_min,age_range_max,age_range,resource_types}")
        assertThat(link.rel).isEqualTo("searchCollections")
        assertThat(link.templated).isEqualTo(true)
    }

    @Test
    fun `my collections contains all collections created by a user`() {
        setSecurityContext("user1", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.myOwnCollections()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/users/user1/collections?bookmarked=false{&projection,page,size,sort_by}")
        assertThat(link.rel).isEqualTo("myCollections")
    }

    @Test
    fun `my saved collections contains all collections created and bookmarked by a user`() {
        setSecurityContext("user1", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.mySavedCollections()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/users/user1/collections?sort_by=UPDATED_AT{&projection,page,size}")
        assertThat(link.rel).isEqualTo("mySavedCollections")
    }

    @Test
    fun `collections of a user when not authenticated`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.myOwnCollections()

        assertThat(link).isNull()
    }

    @Test
    fun `createCollection when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.INSERT_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.createCollection()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/collections")
        assertThat(link.rel).isEqualTo("createCollection")
    }

    @Test
    fun `createCollection when not authenticated`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.createCollection()

        assertThat(link).isNull()
    }

    @Test
    fun `collection when not authenticated`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collection(id = "c123")!!

        assertThat(link).isNotNull
    }

    @Test
    fun `collection when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collection(id = "c123")!!

        assertThat(link.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("collection")
    }

    @Test
    fun `when templated collection`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.collection(id = null)!!

        assertThat(link.href).isEqualTo("https://localhost/v1/collections/{id}{?referer,shareCode}")
        assertThat(link.rel).isEqualTo("collection")
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `when edit and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link =
            collectionsLinkBuilder.editCollection(
                collection = TestFactories.createCollection(
                    id = CollectionId("c123"),
                    owner = user.id.value,
                    bookmarks = setOf(user.id)
                ),
                user = user
            )

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("edit")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `when remove and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link =
            collectionsLinkBuilder.removeCollection(
                collection = TestFactories.createCollection(
                    id = CollectionId("c123"),
                    owner = user.id.value,
                    bookmarks = setOf(user.id)
                ),
                user = user
            )

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123")
        assertThat(link.rel).isEqualTo("remove")
        assertThat(link.templated).isFalse()
    }

    @Test
    fun `when addVideo and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)
        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.addVideoToCollection(
            collection = TestFactories.createCollection(
                id = CollectionId("c123"),
                owner = user.id.value,
                bookmarks = setOf(user.id)
            ),
            user = user
        )

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123/videos/{video_id}")
        assertThat(link.rel).isEqualTo("addVideo")
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `when removeVideo and owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=hello"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)
        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.removeVideoFromCollection(
            collection = TestFactories.createCollection(
                id = CollectionId("c123"),
                owner = user.id.value,
                bookmarks = setOf(user.id)
            ),
            user = user
        )

        assertThat(link!!.href).isEqualTo("https://localhost/v1/collections/c123/videos/{video_id}")
        assertThat(link.rel).isEqualTo("removeVideo")
        assertThat(link.templated).isTrue()
    }

    @Test
    fun `when edit and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.editCollection(
            collection = TestFactories.createCollection(
                id = CollectionId("c123")
            ),
            user = UserFactory.sample()
        )

        assertThat(link).isNull()
    }

    @Test
    fun `when remove and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.removeCollection(
            collection = TestFactories.createCollection(id = CollectionId("c123")),
            user = UserFactory.sample()
        )

        assertThat(link).isNull()
    }

    @Test
    fun `when addVideo and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link =
            collectionsLinkBuilder.addVideoToCollection(
                collection = TestFactories.createCollection(id = CollectionId("c123")),
                user = UserFactory.sample()
            )

        assertThat(link).isNull()
    }

    @Test
    fun `when removeVideo and not owner`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1?q=test"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link =
            collectionsLinkBuilder.removeVideoFromCollection(
                collection = TestFactories.createCollection(id = CollectionId("c123")),
                user = UserFactory.sample()
            )

        assertThat(link).isNull()
    }

    @Test
    fun `when self link`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=true&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.self()

        assertThat(link.href).isEqualTo("https://localhost/v1/collections?projection=list&discoverable=true&page=0&size=2")
        assertThat(link.rel).isEqualTo("self")
    }

    @Test
    fun `when interaction link`() {
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock())

        val link = collectionsLinkBuilder.interactedWith(TestFactories.createCollection(id = CollectionId("c123")))

        assertThat(link.href).contains("/v1/collections/c123/events")
        assertThat(link.templated).isEqualTo(false)
        assertThat(link.rel).isEqualTo("interactedWith")
    }

    @Test
    fun `when details`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.projections().details()

        val url = URL(link.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "details")
        assertThat(url).hasNoParameter("projection", "list")
        assertThat(url).hasParameter("discoverable", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link.rel).isEqualTo("details")
    }

    @Test
    fun `when list`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=details&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.projections().list()

        val url = URL(link.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasNoParameter("projection", "details")
        assertThat(url).hasParameter("discoverable", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link.rel).isEqualTo("list")
    }

    @Test
    fun `when next link and more pages`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(true, 1001, PageRequest(page = 0, size = 1000)))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasParameter("discoverable", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "1")
        assertThat(url).hasNoParameter("page", "0")
        assertThat(url).hasParameter("size", "2")

        assertThat(link?.rel).isEqualTo("next")
    }

    @Test
    fun `when next link and no more pages`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(false, 1, PageRequest(page = 0, size = 1000)))

        assertThat(link).isNull()
    }

    @Test
    fun `when next link and no page parameter sets second page`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val link = collectionsLinkBuilder.next(PageInfo(true, 1001, PageRequest(page = 0, size = 1000)))

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections")
        assertThat(url).hasParameter("projection", "list")
        assertThat(url).hasParameter("discoverable", "false")
        assertThat(url).hasParameter("owner", "pony")
        assertThat(url).hasParameter("page", "1")
        assertThat(url).hasParameter("size", "2")
        assertThat(link?.rel).isEqualTo("next")
    }

    @Test
    fun `bookmark when anonymous`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.bookmark(
            collection = TestFactories.createCollection(
                discoverable = true,
                owner = "another-user"
            ),
            user = user
        )

        assertThat(link).isNull()
    }

    @Test
    fun `bookmark when public and unbookmarked`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_COLLECTIONS)

        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.bookmark(
            collection = TestFactories.createCollection(
                discoverable = true,
                owner = "another-user"
            ),
            user = user
        )

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
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.bookmark(
            collection = TestFactories.createCollection(
                discoverable = false,
                owner = "another-user"
            ),
            user = user
        )

        assertThat(link).isNull()
    }

    @Test
    fun `bookmark when mine`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.bookmark(
            collection = TestFactories.createCollection(
                discoverable = false,
                owner = user.id.value
            ),
            user = user
        )

        assertThat(link).isNull()
    }

    @Test
    fun `bookmark when public but already bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.bookmark(
            collection = TestFactories.createCollection(
                discoverable = false,
                owner = "another-user",
                bookmarks = setOf(user.id)
            ),
            user = user
        )

        assertThat(link).isNull()
    }

    @Test
    fun `unbookmark when public and bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.unbookmark(
            collection = TestFactories.createCollection(
                discoverable = true,
                owner = "another-user",
                bookmarks = setOf(user.id)
            ),
            user = user
        )

        val url = URL(link?.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/collections/collection-id")
        assertThat(url).hasParameter("bookmarked", "false")
        assertThat(link?.rel).isEqualTo("unbookmark")
    }

    @Test
    fun `unbookmark when mine`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.unbookmark(
            collection = TestFactories.createCollection(
                discoverable = true,
                owner = user.id.value,
                bookmarks = setOf(user.id)
            ),
            user = user
        )

        assertThat(link).isNull()
    }

    @Test
    fun `unbookmark when public but not bookmarked`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1/collections?projection=list&discoverable=false&owner=pony&page=0&size=2"))
        val collectionsLinkBuilder = CollectionsLinkBuilder(mock)

        val user = UserFactory.sample()

        val link = collectionsLinkBuilder.unbookmark(
            collection = TestFactories.createCollection(
                discoverable = true,
                owner = "another-user"
            ),
            user = user
        )

        assertThat(link).isNull()
    }
}
