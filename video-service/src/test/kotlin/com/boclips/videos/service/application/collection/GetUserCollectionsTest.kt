package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetUserCollectionsTest {

    lateinit var collectionService: CollectionService
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService

    val video = createVideo()

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { get(listOf(video.asset.assetId)) } doReturn listOf(
                video
            )
        }
        collectionResourceFactory = CollectionResourceFactory(VideoToResourceConverter(), videoService)
    }

    @Test
    fun `user fetches 2 collections with full fat videos`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "me@me.com",
                    title = "collection title",
                    videos = listOf(video.asset.assetId)
                ),
                TestFactories.createCollection()
            )
        }

        val collections = GetUserCollections(
            collectionService,
            collectionResourceFactory
        )(CollectionsController.Projections.details)

        assertThat(collections).hasSize(2)
        val collection = collections.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.videos.first().content.title).isEqualTo(video.asset.title)
    }

    @Test
    fun `user fetches 2 collections with skinny videos`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "me@me.com",
                    title = "collection title",
                    videos = listOf(video.asset.assetId)
                ),
                TestFactories.createCollection()
            )
        }

        val collections =
            GetUserCollections(collectionService, collectionResourceFactory)(CollectionsController.Projections.list)

        assertThat(collections).hasSize(2)
        val collection = collections.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.videos.first().content.id).isEqualTo(video.asset.assetId.value)
        assertThat(collection.videos.first().content.title).isNull()
    }
}