package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.users.client.model.TeacherPlatformAttributes
import com.boclips.users.client.model.User
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun `propagates the OperationForbiddenException when access of collection is not permitted`() {
        val savedCollectionId = saveCollection(public = true)
        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("TEST")))

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
                shareCode = "ABCD",
                referer = "12345"
            )
        }
    }

    @Test
    fun `propagates the OperationForbiddenException when no sharecode or referrer id`() {
        val savedCollectionId = saveCollection(public = true)

        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        setSecurityContext("anon")
        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false)
            )
        }
    }

    @Test
    fun `provides the collection when the correct share code and ID combination is provided`() {
        val savedCollectionId = saveCollection(public = true)
        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )
        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }
}
