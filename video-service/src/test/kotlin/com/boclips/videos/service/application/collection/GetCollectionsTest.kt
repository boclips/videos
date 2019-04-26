package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.Page
import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.domain.model.PageRequest
import com.boclips.videos.service.domain.model.SubjectId
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCollectionsTest {

    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService
    lateinit var collectionService: CollectionService

    val video = TestFactories.createVideo()

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { get(com.nhaarman.mockito_kotlin.any<List<AssetId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        collectionResourceFactory =
            CollectionResourceFactory(VideoToResourceConverter(), SubjectToResourceConverter(), videoService)
    }

    @Test
    fun `fetches all bookmarked collections with skinny videos`() {
        collectionService = mock {
            on { getBookmarked(PageRequest(0, 1), UserId("me@me.com")) } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.asset.assetId),
                        isPublic = true,
                        bookmarks = setOf(UserId("me@me.com"))
                    ),
                    TestFactories.createCollection(
                        isPublic = true,
                        bookmarks = setOf(UserId("me@me.com"))
                    )
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionService, collectionResourceFactory).invoke(
            CollectionFilter(Projection.list, CollectionFilter.Visibility.BOOKMARKED, null, 0, 1)
        )

        assertThat(collections.elements).hasSize(2)
        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.isPublic).isEqualTo(true)
    }

    @Test
    fun `fetches all public collections with skinny videos`() {
        collectionService = mock {
            on { getPublic(PageRequest(0, 1)) } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.asset.assetId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionService, collectionResourceFactory).invoke(
            CollectionFilter(Projection.list, CollectionFilter.Visibility.PUBLIC, null, 0, 1)
        )

        assertThat(collections.elements).hasSize(2)
        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.isPublic).isEqualTo(true)
    }

    @Test
    fun `fetches all public collections with fat videos`() {
        collectionService = mock {
            on { getPublic(PageRequest(0, 1)) } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.asset.assetId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionService, collectionResourceFactory).invoke(
            CollectionFilter(
                projection = Projection.details,
                visibility = CollectionFilter.Visibility.PUBLIC,
                owner = null,
                pageNumber = 0,
                pageSize = 1
            )
        )

        assertThat(collections.elements).hasSize(2)

        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.isPublic).isEqualTo(true)
        assertThat(collection.videos.first().content.title).isEqualTo(video.asset.title)
    }

    @Test
    fun `fetches collections with their subjects`() {
        collectionService = mock {
            on { getPublic(PageRequest(0, 1)) } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.asset.assetId),
                        isPublic = true,
                        subjects = setOf(SubjectId("1"))
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionService, collectionResourceFactory).invoke(
            CollectionFilter(
                projection = Projection.details,
                visibility = CollectionFilter.Visibility.PUBLIC,
                owner = null,
                pageNumber = 0,
                pageSize = 1
            )
        )

        assertThat(collections.elements).hasSize(2)

        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.subjects).hasSize(1)
        val subject = collection.subjects.first()
        assertThat(subject.content.id).isEqualTo("1")
    }
}