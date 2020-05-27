package com.boclips.videos.service.application.collection

import com.boclips.eventbus.domain.user.User
import com.boclips.eventbus.domain.user.UserProfile
import com.boclips.eventbus.events.user.UserCreated
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

class CreateDefaultCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var subject: CreateDefaultCollection

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `when user created event is fired, we create a default collection`() {
        val rawUserId = "someId"
        val user = User.builder()
            .id(rawUserId)
            .createdAt(ZonedDateTime.now())
            .isBoclipsEmployee(false)
            .profile(UserProfile.builder().subjects(listOf()).ages(listOf()).build())
            .build()
        val userCreated = UserCreated.builder().user(user).build()

        fakeEventBus.publish(userCreated)

        collectionRepository
            .streamAll { sequence ->
                val allCollections = sequence.asIterable().toList()

                val userCollection = allCollections.find { collection -> collection.owner.value == rawUserId }
                assertThat(userCollection).isNotNull
                assertThat(userCollection!!.default).isTrue()
            }
    }
}
