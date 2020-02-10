package com.boclips.videos.service.domain.service.collection

import com.boclips.users.client.UserServiceClient
import com.boclips.videos.service.domain.model.AccessError
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionAccessServiceTest {
    lateinit var collectionAccessService: CollectionAccessService
    lateinit var collectionRepository: CollectionRepository

    lateinit var userServiceClient: UserServiceClient

    @BeforeEach
    fun setup() {
        userServiceClient = mock<UserServiceClient>()
        collectionAccessService = CollectionAccessService(userServiceClient)
    }

    @Nested
    inner class WriteAccess {
        @Test
        fun `does not allow user write access to a private collection they do not own`() {
            val privateCollection = TestFactories.createCollection(owner = "innocent@example.com", isPublic = false)

            collectionRepository = mock {
                on { find(privateCollection.id) } doReturn privateCollection
            }

            collectionAccessService = CollectionAccessService(userServiceClient)

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

            collectionAccessService = CollectionAccessService(userServiceClient)

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
    }

    @Nested
    inner class ReadAccess {

        @Nested
        inner class AuthenticatedUser {

            @Nested
            inner class CanAccess {
                @Test
                fun `any public collection`() {
                    val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = publicCollection,
                        user = UserFactory.sample(
                            id = "attacker@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = null, referer = null
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }


                // We do not check shareCodes for authenticated users if the collection is public
                // so they can provide any invalid data, we ignore it
                @Test
                fun `public collection with invalid shareCode`() {
                    val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = publicCollection,
                        user = UserFactory.sample(
                            id = "teacher@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = "INVALID", referer = "owner@example.com"
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }

                @Test
                fun `private collection if requester is owner`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "owner@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = null, referer = null
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }

                @Test
                fun `private collection owned by referer with valid shareCode`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    whenever(userServiceClient.validateShareCode("owner@example.com", "AB12")).thenReturn(true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "teacher@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }),
                        shareCode = "AB12", referer = "owner@example.com"
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }

                @Test
                fun `private collection if user has VIEW_ANY_COLLECTION role`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "superuser@example.com",
                            isPermittedToViewAnyCollection = true,
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = null, referer = null
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }
            }

            @Nested
            inner class CannotAccess {
                @Test
                fun `private collection for non-owners without shareCode`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "attacker@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = null, referer = null
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }

                @Test
                fun `private collection when referer is not owner of collection`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    whenever(userServiceClient.validateShareCode("other@example.com", "ABC123")).thenReturn(true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "attacker@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = "ABC123", referer = "other@example.com"
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }

                @Test
                fun `private collection when shareCode is invalid`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    whenever(userServiceClient.validateShareCode("other@example.com", "ABC123")).thenReturn(false)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "attacker@example.com",
                            accessRulesSupplier = { AccessRulesFactory.publicOnly() }
                        ),
                        shareCode = "ABC123", referer = "owner@example.com"
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }
            }
        }

        @Nested
        inner class NonAuthenticatedUser {
            @Nested
            inner class CanAccess {

                @Test
                fun `public collection with a valid shareCode`() {
                    val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

                    whenever(userServiceClient.validateShareCode("teacher@example.com", "AB12")).thenReturn(true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = publicCollection,
                        user = UserFactory.sample(
                            id = "unauthenticated@example.com",
                            isAuthenticated = false,
                            accessRulesSupplier = { AccessRules.anonymousAccess() }
                        ),
                        shareCode = "AB12", referer = "teacher@example.com"
                    )

                    assertThat(accessValidationResult.successful).isTrue()
                }

                @Test
                fun `private collection owned by referer with a valid shareCode`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    whenever(userServiceClient.validateShareCode("owner@example.com", "AB12")).thenReturn(true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "unauthenticated@example.com",
                            isAuthenticated = false,
                            accessRulesSupplier = { AccessRules.anonymousAccess() }
                        ),
                        shareCode = "AB12", referer = "owner@example.com"
                    )
                    assertThat(accessValidationResult.successful).isTrue()
                }
            }

            @Nested
            inner class CannotAccess {
                @Test
                fun `public collection without shareCode`() {
                    val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = publicCollection,
                        user = UserFactory.sample(
                            id = "unauthenticated@example.com",
                            isAuthenticated = false,
                            accessRulesSupplier = { AccessRules.anonymousAccess() }
                        ),
                        shareCode = null, referer = null
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }

                @Test
                fun `public collection with invalid shareCode`() {
                    val publicCollection = TestFactories.createCollection(owner = "owner@example.com", isPublic = true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = publicCollection,
                        user = UserFactory.sample(
                            id = "unauthenticated@example.com",
                            isAuthenticated = false,
                            accessRulesSupplier = { AccessRules.anonymousAccess() }
                        ),
                        shareCode = "INVALID", referer = "owner@example.com"
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }

                @Test
                fun `private collection when referer is not owner of collection`() {
                    val privateCollection =
                        TestFactories.createCollection(owner = "owner@example.com", isPublic = false)

                    whenever(userServiceClient.validateShareCode("teacher@example.com", "AB12")).thenReturn(true)

                    val accessValidationResult = collectionAccessService.validateReadAccess(
                        collection = privateCollection,
                        user = UserFactory.sample(
                            id = "unauthenticated@example.com",
                            isAuthenticated = false,
                            accessRulesSupplier = { AccessRules.anonymousAccess() }
                        ),
                        shareCode = "AB12", referer = "teacher@example.com"
                    )

                    assertThat(accessValidationResult.successful).isFalse()
                    assertThat(accessValidationResult.error!! is AccessError.InvalidShareCode).isTrue()
                }
            }
        }
    }
}
