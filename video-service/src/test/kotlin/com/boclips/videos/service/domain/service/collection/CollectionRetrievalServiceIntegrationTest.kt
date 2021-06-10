package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.domain.ResourceType
import com.boclips.eventbus.events.resource.ResourcesSearched
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionRetrievalServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionRetrievalService: CollectionRetrievalService

    @Nested
    inner class SearchingForCollections {
        @Test
        fun `retrieved collections filter out videos according to access rules`() {
            val firstPermittedId = saveVideo()
            val secondPermittedId = saveVideo()

            val nonPermittedId = saveVideo()

            saveCollection(
                title = "a collection",
                videos = listOf(
                    firstPermittedId.value,
                    nonPermittedId.value
                )
            )

            val pagedCollectionResult = collectionRetrievalService.search(
                query = CollectionSearchQuery(
                    text = "a collection",
                    pageSize = 1,
                    pageIndex = 0,
                    subjectIds = emptyList(),
                    permittedCollections = null,
                    discoverable = false,
                    hasLessonPlans = null
                ),

                user = UserFactory.sample(
                    accessRulesSupplier = {
                        AccessRulesFactory.sample(
                            videoAccess = VideoAccess.Rules(
                                listOf(
                                    VideoAccessRule.IncludedIds(
                                        setOf(
                                            firstPermittedId,
                                            secondPermittedId
                                        )
                                    )
                                )
                            , emptySet())
                        )
                    }
                )
            )

            assertThat(pagedCollectionResult.elements).hasSize(1)
            assertThat(pagedCollectionResult.elements.first().videos).containsExactly(firstPermittedId)
        }

        @Test
        fun `when searching collections filtered by age range`() {
            saveCollection(
                title = "pre-school collection",
                videos = listOf(
                    TestFactories.createVideoId().value,
                    TestFactories.createVideoId().value
                ),
                ageRangeMin = 3,
                ageRangeMax = 5
            )
            val lowerElementaryCollectionId: CollectionId = saveCollection(
                title = "lower-elementary collection",
                videos = listOf(
                    TestFactories.createVideoId().value,
                    TestFactories.createVideoId().value
                ),
                ageRangeMin = 5,
                ageRangeMax = 7
            )

            val collectionSearchQuery = CollectionSearchQuery(
                text = "collection",
                pageSize = 5,
                pageIndex = 0,
                subjectIds = emptyList(),
                permittedCollections = null,
                discoverable = false,
                hasLessonPlans = null,
                ageRangeMin = 5,
                ageRangeMax = 7
            )

            val results = collectionRetrievalService.search(
                query = collectionSearchQuery,
                user = UserFactory.sample(id = "user-id-34")
            )

            assertThat(results.elements).hasSize(1)
            assertThat(results.elements.elementAt(0).id).isEqualTo(lowerElementaryCollectionId)
        }

        @Test
        fun `when searching collections it sends a ResourcesSearched event`() {
            val collectionId = saveCollection(
                title = "a collection",
                videos = listOf(
                    TestFactories.createVideoId().value,
                    TestFactories.createVideoId().value
                )
            )

            val collectionSearchQuery = CollectionSearchQuery(
                text = "a collection",
                pageSize = 1,
                pageIndex = 0,
                subjectIds = emptyList(),
                permittedCollections = null,
                discoverable = false,
                hasLessonPlans = null
            )

            collectionRetrievalService.search(
                query = collectionSearchQuery,
                user = UserFactory.sample(id = "user-id-34"),
                queryParams = mapOf("age_range" to listOf("4-6"))
            )

            val event = fakeEventBus.getEventOfType(ResourcesSearched::class.java)
            assertThat(event.query).isEqualTo("a collection")
            assertThat(event.queryParams["age_range"]).containsExactly("4-6")
            assertThat(event.resourceType).isEqualTo(ResourceType.COLLECTION)
            assertThat(event.pageIndex).isEqualTo(0)
            assertThat(event.pageSize).isEqualTo(1)
            assertThat(event.totalResults).isEqualTo(1)
            assertThat(event.pageResourceIds).containsExactly(collectionId.value)
            assertThat(event.userId).isEqualTo("user-id-34")
        }
    }

    @Nested
    inner class FindCollection {
        @Test
        fun `can find a collection by ID`() {
            val videoId = saveVideo()
            val collectionId = saveCollection(videos = listOf(videoId.value), discoverable = true)
            val collection = collectionRetrievalService.findAnyCollection(
                collectionId,
                UserFactory.sample()
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).containsExactly(videoId)
        }

        @Test
        fun `can find a collection by ID with only permitted videos included`() {
            val firstPermittedId = saveVideo()
            val secondPermittedId = saveVideo()

            val nonPermittedId = saveVideo()

            val collectionId = saveCollection(
                videos = listOf(
                    firstPermittedId.value,
                    nonPermittedId.value
                )
            )

            val collection = collectionRetrievalService.findAnyCollection(
                collectionId,
                UserFactory.sample(
                    accessRulesSupplier = {
                        AccessRulesFactory.sample(
                            videoAccess = VideoAccess.Rules(
                                listOf(
                                    VideoAccessRule.IncludedIds(
                                        videoIds = setOf(
                                            firstPermittedId,
                                            secondPermittedId
                                        )
                                    )
                                )
                            , emptySet())
                        )
                    }
                )
            )!!

            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).containsExactly(firstPermittedId)
        }

        @Test
        fun `cannot find missing collection by ID`() {
            assertThat(
                collectionRetrievalService.findAnyCollection(
                    CollectionId("nonexistent"),
                    UserFactory.sample()
                )
            ).isNull()
        }

        @Test
        @Disabled("At the moment we do not restrict access to specific collections")
        fun `cannot find collection that access rules do not permit`() {
            val collectionId = saveCollection(discoverable = false, owner = "joe")
            val user = UserFactory.sample(
                id = "catherine",
                accessRulesSupplier = {
                    AccessRulesFactory.sample(
                        collectionAccessRule = CollectionAccessRule.specificIds(emptyList())
                    )
                }
            )

            val collection = collectionRetrievalService.findAnyCollection(
                id = collectionId,
                user = user
            )

            assertThat(collection).isNull()
        }

        @Test
        fun `can find a collection by ID without adding videos to it`() {
            val videoId = saveVideo()
            val collectionId = saveCollection(videos = listOf(videoId.value), discoverable = true)
            val collection = collectionRetrievalService.findAnyCollection(
                id = collectionId,
                user = UserFactory.sample(),
                populateVideos = false
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).isEmpty()
        }
    }

    @Nested
    inner class FindWritableCollection {
        @Test
        fun `can find collection that we have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionRetrievalService.findOwnCollection(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = true)
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
        }

        @Test
        fun `cannot find collection if we don't have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionRetrievalService.findOwnCollection(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = false)
            )

            assertThat(collection).isNull()
        }
    }
}
