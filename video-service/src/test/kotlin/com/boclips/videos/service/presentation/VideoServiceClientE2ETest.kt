package com.boclips.videos.service.presentation

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.TestTokenFactory
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoServiceClientE2ETest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class Videos {
        @Test
        fun `can fetch a video`() {
            val savedVideo = saveVideo()

            val videosClient =
                VideosClient.create(apiUrl = "http://localhost:$randomServerPort", objectMapper = objectMapper)

            assertThat(videosClient.getVideo(savedVideo.value)).isNotNull
        }
    }

    @Nested
    inner class Collections {
        @Test
        fun `can fetch a collection`() {
            val savedCollection = saveCollection(owner = "the@owner.com")

            val collectionsClient = CollectionsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory("the@owner.com", UserRoles.VIEW_COLLECTIONS)
            )

            assertThat(collectionsClient.getCollection(savedCollection.value)).isNotNull
        }
    }
}
