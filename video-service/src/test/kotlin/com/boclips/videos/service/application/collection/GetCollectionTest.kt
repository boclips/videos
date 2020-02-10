package com.boclips.videos.service.application.collection

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
    fun `propagates the OperationForbiddenException for public collection with invalid shareCode`() {
        val savedCollectionId = saveCollection(public = true)
        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("TEST")))

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
                shareCode = "INVALID",
                referer = "12345"
            )
        }
    }

    @Test
    fun `propagates the OperationForbiddenException for private collection without share code`() {
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            public = false
        )

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "attacker@example.com")
            )
        }
    }

    @Test
    fun `propagates the OperationForbiddenException for anonymous no sharecode or referrer id`() {
        val savedCollectionId = saveCollection()

        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false)
            )
        }
    }

    @Test
    fun `provides a public collection when the correct share code and ID combination is provided`() {
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

    @Test
    fun `unauthenticated user has access to a private collection when the correct share code and referrer combination is provided`() {
        val savedCollectionId = saveCollection(owner = "12345", public = false)
        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )
        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }

    @Test
    fun `authenticated user has access to a private collection when the correct share code and referrer combination is provided`() {
        val savedCollectionId = saveCollection(owner = "12345", public = false)
        userServiceClient.addUser(User("12345", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )
        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }

    @Test
    fun `user does not have access to private collection when referer id is not the owner of the collection`() {
        val savedCollectionId = saveCollection(owner = "the-owner", public = false)
        userServiceClient.addUser(User("not-the-owner", null, emptyList(), TeacherPlatformAttributes("ABCD")))

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
                shareCode = "ABCD",
                referer = "not-the-owner"
            )
        }
    }
}
