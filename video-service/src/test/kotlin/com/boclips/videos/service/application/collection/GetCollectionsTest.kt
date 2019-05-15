package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.SubjectId
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCollectionsTest {

    lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService
    lateinit var collectionRepository: CollectionRepository
    lateinit var videosLinkBuilder: VideosLinkBuilder

    val video = TestFactories.createVideo()

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { getPlayableVideo(com.nhaarman.mockito_kotlin.any<List<VideoId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        videosLinkBuilder = mock()
        playbackToResourceConverter = mock()
        collectionResourceFactory =
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                videoService
            )
    }

    @Test
    fun `fetches all bookmarked collections with skinny videos`() {
        collectionRepository = mock {
            on {
                getBookmarked(
                    PageRequest(0, 1),
                    UserId("me@me.com")
                )
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
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

        val collections = GetCollections(collectionRepository, collectionResourceFactory).invoke(
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
        collectionRepository = mock {
            on {
                getPublic(
                    PageRequest(
                        0,
                        1
                    )
                )
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionRepository, collectionResourceFactory).invoke(
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
        collectionRepository = mock {
            on {
                getPublic(
                    PageRequest(
                        0,
                        1
                    )
                )
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionRepository, collectionResourceFactory).invoke(
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
        assertThat(collection.videos.first().content.title).isEqualTo(video.title)
    }

    @Test
    fun `fetches collections with their subjects`() {
        collectionRepository = mock {
            on {
                getPublic(
                    PageRequest(
                        0,
                        1
                    )
                )
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true,
                        subjects = setOf(SubjectId("1"))
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true)
            )
        }

        val collections = GetCollections(collectionRepository, collectionResourceFactory).invoke(
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