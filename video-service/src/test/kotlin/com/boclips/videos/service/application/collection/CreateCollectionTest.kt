package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

class CreateCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    @WithMockUser("this-user")
    fun `creates collection and associates videos`() {
        val videoId1 = createVideo(TestFactories.createCreateVideoRequest(title = "a-video", providerVideoId = "hurray", playbackId = "hiphip")).content.id
        val videoId2 = createVideo(TestFactories.createCreateVideoRequest(title = "another-video")).content.id
        val createRequest = TestFactories.createCollectionRequest(
            title = "title",
            videos = listOf("http://localhost/v1/videos/$videoId1", "http://localhost/v1/videos/$videoId2")
        )

        val collection = createCollection(createRequest)

        val allCollections = collectionService.getByOwner(UserId("this-user"))
        assertThat(allCollections).contains(collection)
        assertThat(collection.title).isEqualTo("title")
        assertThat(collection.owner.value).isEqualTo("this-user")
        assertThat(collection.videos).hasSize(2)
        assertThat(collection.videos.first().asset.title).isEqualTo("a-video")
    }

    @Test
    fun `throws when missing title`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = null,
            videos = listOf("http://localhost/v1/videos/a-video")
        )

        assertThrows<NonNullableFieldCreateRequestException> { createCollection(createRequest) }
    }

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                referenceId = "123"
            )
        )
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                referenceId = "hiphip"
            )
        )
    }
}
