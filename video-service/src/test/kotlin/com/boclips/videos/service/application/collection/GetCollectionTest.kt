package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetCollectionTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getCollection: GetCollection

    @Test
    fun `propagates CollectionNotFound error thrown from downstream`() {
        assertThrows<CollectionNotFoundException> {
            getCollection(
                collectionId = "123",
                user = UserFactory.sample()
            )
        }
    }

    @Test
    fun `propagates the CollectionNotFoundException when access of collection is not permitted`() {
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            public = false
        )

        assertThrows<CollectionNotFoundException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "attacker@example.com")
            )
        }
    }
}
