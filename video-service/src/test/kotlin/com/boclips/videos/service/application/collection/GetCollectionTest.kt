package com.boclips.videos.service.application.collection

import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.user.TeacherPlatformAttributesResource
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
        usersClient.add(
            UserResourceFactory.sample(
                id = "12345",
                teacherPlatformAttributes = TeacherPlatformAttributesResource("TEST")
            )
        )

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
                collectionId = savedCollectionId.value,
                user = UserFactory.sample(id = "attacker@example.com", isAuthenticated = false)
            )
        }
    }

    @Test
    fun `propagates the OperationForbiddenException for anonymous no sharecode or referrer id`() {
        val savedCollectionId = saveCollection()

        usersClient.add(
            UserResourceFactory.sample(
                id = "12345",
                teacherPlatformAttributes = TeacherPlatformAttributesResource("ABCD")
            )
        )

        assertThrows<OperationForbiddenException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "anonymous", isAuthenticated = false)
            )
        }
    }

    @Test
    fun `provides a public collection when the correct share code and ID combination is provided`() {
        val savedCollectionId = saveCollection(public = true, owner = "12345")
        usersClient.add(
            UserResourceFactory.sample(
                id = "12345",
                teacherPlatformAttributes = TeacherPlatformAttributesResource("ABCD")
            )
        )

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymous", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )
        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }

    @Test
    fun `unauthenticated user has access to a private collection with correct share code and referrer`() {
        val savedCollectionId = saveCollection(owner = "12345", public = false)
        usersClient.add(
            UserResourceFactory.sample(
                id = "12345",
                teacherPlatformAttributes = TeacherPlatformAttributesResource("ABCD")
            )
        )

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymousUser", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )

        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }

    @Test
    fun `authenticated user has access to a private collection with  correct share code and referrer`() {
        val savedCollectionId = saveCollection(owner = "12345", public = false)
        usersClient.add(
            UserResourceFactory.sample(
                id = "12345",
                teacherPlatformAttributes = TeacherPlatformAttributesResource("ABCD")
            )
        )

        val retrievedCollection = getCollection(
            savedCollectionId.value,
            user = UserFactory.sample(id = "anonymousUser", isAuthenticated = false),
            shareCode = "ABCD",
            referer = "12345"
        )

        assertThat(retrievedCollection.id.value).isEqualTo(savedCollectionId.value)
    }
}
