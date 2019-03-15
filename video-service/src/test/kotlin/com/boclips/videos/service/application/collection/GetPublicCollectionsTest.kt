package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPublicCollectionsTest {

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
        collectionResourceFactory = CollectionResourceFactory(VideoToResourceConverter(), videoService)
    }

    @Test
    fun `fetches all public collections with skinny videos`() {
        collectionService = mock {
            on { getPublic() } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "yoyoyo@public.com",
                    title = "collection title",
                    videos = listOf(video.asset.assetId),
                    isPublic = true
                ),
                TestFactories.createCollection(isPublic = true)
            )
        }

        val collections = GetPublicCollections(collectionService, collectionResourceFactory)(CollectionsController.Projections.list)

        assertThat(collections).hasSize(2)
        val collection = collections.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.isPublic).isEqualTo(true)
    }

    @Test
    fun `fetches all public collections with fat videos`() {
        collectionService = mock {
            on { getPublic() } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "yoyoyo@public.com",
                    title = "collection title",
                    videos = listOf(video.asset.assetId),
                    isPublic = true
                ),
                TestFactories.createCollection(isPublic = true)
            )
        }

        val collections = GetPublicCollections(collectionService, collectionResourceFactory)(CollectionsController.Projections.details)

        assertThat(collections).hasSize(2)
        val collection = collections.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.isPublic).isEqualTo(true)
        assertThat(collection.videos.first().content.title).isEqualTo(video.asset.title)
    }
}