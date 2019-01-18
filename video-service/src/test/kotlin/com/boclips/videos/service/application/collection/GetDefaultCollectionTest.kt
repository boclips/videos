package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDefaultCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `user does not have a collection`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn emptyList<Collection>()
        }
        val getDefaultCollection = GetDefaultCollection(collectionService, VideoToResourceConverter())

        val collection = getDefaultCollection.execute()

        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEmpty()
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `user has a collection`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(TestFactories.createCollection(
                    owner = "me@me.com",
                    title = "collection title",
                    videos = listOf(createVideo())
            ))
        }
        val getDefaultCollection = GetDefaultCollection(collectionService, VideoToResourceConverter())

        val collection = getDefaultCollection.execute()

        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
    }
}