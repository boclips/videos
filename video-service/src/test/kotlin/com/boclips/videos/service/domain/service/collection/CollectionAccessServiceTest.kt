package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollectionAccessServiceTest {
    lateinit var collectionAccessService: CollectionAccessService
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `does not allow user write access to a private collection they do not own`() {
        val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

        collectionRepository = mock {
            on { find(privateCollection.id) } doReturn privateCollection
        }

        collectionAccessService = CollectionAccessService()

        val hasWriteAccess = collectionAccessService.hasWriteAccess(
            collection = privateCollection,
            user = UserFactory.sample(id = "attacker@example.com", accessRulesSupplier = {
                AccessRules(
                    CollectionAccessRule.specificIds(listOf()),
                    VideoAccessRule.Everything
                )
            })
        )

        assertThat(hasWriteAccess).isFalse()
    }

    @Test
    fun `does not allow user write access to a public collection they do not own`() {
        val publicCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        collectionAccessService = CollectionAccessService()

        val hasWriteAccess = collectionAccessService.hasWriteAccess(
            collection = publicCollection,
            user = UserFactory.sample(id = "attacker@example.com", accessRulesSupplier = {
                AccessRules(
                    CollectionAccessRule.public(),
                    VideoAccessRule.Everything
                )
            })
        )

        assertThat(hasWriteAccess).isFalse()
    }

    @Test
    fun `allows any teacher to access public collection`() {
        val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

        collectionRepository = mock {
            on { find(publicCollection.id) } doReturn publicCollection
        }

        collectionAccessService = CollectionAccessService()

        val hasReadAccess = collectionAccessService.hasReadAccess(
            collection = publicCollection,
            user = UserFactory.sample(
                id = "attacker@example.com",
                accessRulesSupplier = {
                    AccessRules(
                        videoAccess = VideoAccessRule.Everything,
                        collectionAccess = CollectionAccessRule.public()
                    )
                })
        )

        assertThat(hasReadAccess).isTrue()
    }
}
