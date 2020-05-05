package com.boclips.videos.service.application.collection

import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class RemoveVideoFromCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var removeVideoFromCollection: RemoveVideoFromCollection

    @Test
    fun `throws when collection id or video id is null`() {
        assertThrows<Exception> {
            removeVideoFromCollection(
                null,
                "abc",
                UserFactory.sample(id = "owner@collections.com")
            )
        }

        assertThrows<Exception> {
            removeVideoFromCollection(
                "abc",
                null,
                UserFactory.sample(id = "owner@collections.com")
            )
        }
    }
}
