package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.domain.ResourceType
import com.boclips.eventbus.events.resource.ResourcesSearched
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.user.TeacherPlatformAttributesResource
import com.boclips.videos.api.request.attachments.AttachmentRequest
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
                title = "a collection", videos = listOf(
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
                    visibilityForOwners = setOf(
                        VisibilityForOwner(
                            owner = null,
                            visibility = CollectionVisibilityQuery.privateOnly()
                        )
                    ),
                    hasLessonPlans = null
                ),

                user = UserFactory.sample(accessRulesSupplier = {
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
                        )
                    )
                })
            )

            assertThat(pagedCollectionResult.elements).hasSize(1)
            assertThat(pagedCollectionResult.elements.first().videos).containsExactly(firstPermittedId)
        }

        @Test
        fun `when searching collections filtered by age range`() {
            saveCollection(
                title = "pre-school collection", videos = listOf(
                    TestFactories.createVideoId().value,
                    TestFactories.createVideoId().value
                ),
                ageRangeMin = 3,
                ageRangeMax = 5
            )
            val lowerElementaryCollectionId: CollectionId = saveCollection(
                title = "lower-elementary collection", videos = listOf(
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
                visibilityForOwners = setOf(
                    VisibilityForOwner(
                        owner = null,
                        visibility = CollectionVisibilityQuery.privateOnly()
                    )
                ),
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
                title = "a collection", videos = listOf(
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
                visibilityForOwners = setOf(
                    VisibilityForOwner(
                        owner = null,
                        visibility = CollectionVisibilityQuery.privateOnly()
                    )
                ),
                hasLessonPlans = null
            )

            collectionRetrievalService.search(
                query = collectionSearchQuery,
                user = UserFactory.sample(id = "user-id-34")
            )

            val event = fakeEventBus.getEventOfType(ResourcesSearched::class.java)
            assertThat(event.query).isEqualTo("a collection")
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
            val collectionId = saveCollection(videos = listOf(videoId.value), public = true)
            val collection = collectionRetrievalService.find(
                collectionId,
                UserFactory.sample()
            ).collection!!
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

            val collection = collectionRetrievalService.find(
                collectionId, UserFactory.sample(accessRulesSupplier = {
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
                        )
                    )
                })
            ).collection!!

            assertThat(collection.id).isEqualTo(collectionId)
            assertThat(collection.videos).containsExactly(firstPermittedId)
        }

        @Test
        fun `cannot find missing collection by ID`() {

            assertThat(
                collectionRetrievalService.find(
                    CollectionId("nonexistent"),
                    UserFactory.sample()
                ).collection
            ).isNull()
        }

        @Test
        fun `cannot find collection that access rules do not permit`() {
            val collectionId = saveCollection()
            assertThat(
                collectionRetrievalService.find(
                    collectionId, UserFactory.sample(accessRulesSupplier = {
                        AccessRulesFactory.sample(
                            collectionAccessRule = CollectionAccessRule.specificIds(
                                emptyList()
                            )
                        )
                    })
                ).collection
            ).isNull()
        }

        @Test
        fun `collection does not contain attachments for unauthenticated users with share code`() {
            val collectionId = saveCollection(
                owner = "12345", public = false, attachment = AttachmentRequest(
                    linkToResource = "www.lesson-plan.com",
                    description = "new description",
                    type = "LESSON_PLAN"
                )
            )
            usersClient.add(
                UserResourceFactory.sample(
                    id = "12345",
                    teacherPlatformAttributes = TeacherPlatformAttributesResource("ABCD")
                )
            )

            val collection = collectionRetrievalService.find(
                collectionId,
                UserFactory.sample(isAuthenticated = false),
                "12345",
                "ABCD"

            ).collection!!

            assertThat(collection.attachments.isEmpty()).isTrue()
        }
    }

    @Nested
    inner class FindWritableCollection {
        @Test
        fun `can find collection that we have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionRetrievalService.findWritable(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = true)
            )!!
            assertThat(collection.id).isEqualTo(collectionId)
        }

        @Test
        fun `cannot find collection if we don't have write access to`() {
            val collectionId = saveCollection()
            val collection = collectionRetrievalService.findWritable(
                collectionId,
                UserFactory.sample(isPermittedToViewAnyCollection = false)
            )

            assertThat(collection).isNull()
        }
    }
}
