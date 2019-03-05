package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
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
    lateinit var collectionResourceConverter: CollectionResourceConverter
    lateinit var videoService: VideoService

    val assetId = createVideo().asset.assetId

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { get(listOf(assetId)) } doReturn listOf(
                createVideo()
            )
        }
        collectionResourceConverter = CollectionResourceConverter(VideoToResourceConverter(), videoService)
    }

    @Test
    fun `user has two collections`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "me@me.com",
                    title = "collection title",
                    videos = listOf(assetId)
                ),
                TestFactories.createCollection()
            )
        }
        val collections = GetUserCollections(collectionService, collectionResourceConverter)()

        assertThat(collections).hasSize(2)
        val collection = collections.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
    }
}