package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class CollectionReadServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionReadService: CollectionReadService

    @Nested
    inner class SearchingForCollections {
        @Test
        fun `retrieved collections filter out videos according to access rules`() {
            val firstPermittedId = TestFactories.createVideoId()
            val secondPermittedId = TestFactories.createVideoId()

            val nonPermittedId = TestFactories.createVideoId()

            saveCollection(
                title = "a collection", videos = listOf(
                    firstPermittedId.value,
                    nonPermittedId.value
                )
            )

            val pagedCollectionResult = collectionReadService.search(
                query = CollectionSearchQuery(
                    text = "a collection",
                    pageSize = 1,
                    pageIndex = 0,
                    subjectIds = emptyList(),
                    permittedCollections = null,
                    visibilityForOwners = setOf(
                        VisibilityForOwner(
                            owner = null,
                            visibility = CollectionVisibilityQuery.privateOnly()
                        )
                    ),
                    hasLessonPlans = null
                ),
                accessRules = AccessRulesFactory.sample(
                    videoAccessRule = VideoAccessRule.SpecificIds(
                        setOf(
                            firstPermittedId,
                            secondPermittedId
                        )
                    )
                )
            )

            assertThat(pagedCollectionResult.elements).hasSize(1)
            assertThat(pagedCollectionResult.elements.first().videos).containsExactly(firstPermittedId)
        }
    }

    @Nested
    inner class FindCollection {
        @Test
        fun `can find a collection by ID`() {
            val videoId = TestFactories.createVideoId()
            val collectionId = saveCollection(videos = listOf(videoId.value), public = true)
            val collection = collectionReadService.find(
                collectionId,
                UserFactory.sample()
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).containsExactly(videoId)
        }

        @Test
        fun `can find a collection by ID with only permitted videos included`() {
            val firstPermittedId = TestFactories.createVideoId()
            val secondPermittedId = TestFactories.createVideoId()

            val nonPermittedId = TestFactories.createVideoId()

            val collectionId = saveCollection(
                videos = listOf(
                    firstPermittedId.value,
                    nonPermittedId.value
                )
            )

            val collection = collectionReadService.find(
                collectionId, UserFactory.sample(accessRulesSupplier = {
                    AccessRulesFactory.sample(
                        videoAccessRule = VideoAccessRule.SpecificIds(
                            videoIds = setOf(
                                firstPermittedId,
                                secondPermittedId
                            )
                        )
                    )
                })
            )!!

            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).containsExactly(firstPermittedId)
        }

        @Test
        fun `cannot find missing collection by ID`() {
            assertThat(
                collectionReadService.find(
                    CollectionId("nonexistent"),
                    UserFactory.sample()
                )
            ).isNull()
        }

        @Test
        fun `cannot find collection that access rules do not permit`() {
            val collectionId = saveCollection()
            assertThat(
                collectionReadService.find(
                    collectionId, UserFactory.sample(accessRulesSupplier = {
                        AccessRulesFactory.sample(
                            collectionAccessRule = CollectionAccessRule.specificIds(
                                emptyList()
                            )
                        )
                    })
                )
            ).isNull()
        }
    }

    @Nested
    inner class FindWritableCollection {
        @Test
        fun `can find collection that we have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionReadService.findWritable(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = true)
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
        }

        @Test
        fun `cannot find collection if we don't have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionReadService.findWritable(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = false)
            )

            assertThat(collection).isNull()
        }
    }
}