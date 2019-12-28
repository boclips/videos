package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery.Companion.privateOnly
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery.Companion.publicOnly
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionSearchQueryAssemblerTest {

    @Test
    fun `assembles a query with all criteria`() {
        val query = collectionSearchQueryAssembler(
            query = "minute physics",
            subjects = listOf("Physics"),
            bookmarked = true,
            public = true,
            page = 1,
            size = 30,
            user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("my-id") }),
            owner = "other-id",
            hasLessonPlans = true
        )

        assertThat(query.text).isEqualTo("minute physics")
        assertThat(query.subjectIds).containsOnly("Physics")
        assertThat(query.visibilityForOwners).containsExactly(VisibilityForOwner("other-id", publicOnly()))
        assertThat(query.bookmarkedBy).isEqualTo("my-id")
        assertThat(query.pageIndex).isEqualTo(1)
        assertThat(query.pageSize).isEqualTo(30)
        assertThat(query.hasLessonPlans).isTrue()
    }

    @Test
    fun `assembles a query with specific ID access`() {
        val collectionIds = arrayOf(CollectionId("1"), CollectionId("2"))

        val query = collectionSearchQueryAssembler(
            user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.specificIds(*collectionIds) })
        )

        assertThat(query.permittedCollections).containsExactlyInAnyOrder(*collectionIds)
    }

    @Nested
    inner class `visibility for owners` {
        @Test
        fun `can access other owner's public collections`() {
            val query = collectionSearchQueryAssembler(
                public = true,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `cannot access other owner's private collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    public = false,
                    owner = "other-folk",
                    user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
                )
            }
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `gets all public collections and owned private collections if no visibility and owner specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = null,
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly()),
                VisibilityForOwner(owner = "me", visibility = privateOnly())
            )
        }
    }

    @Nested
    inner class `visibility for public-only access` {
        @Test
        fun `can access other owner's public collections`() {
            val query = collectionSearchQueryAssembler(
                public = true,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `cannot access private collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    public = false,
                    user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
                )
            }
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `gets all public collections if no visibility and owner specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = null,
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly())
            )
        }
    }

    @Nested
    inner class `visibility for superusers` {
        @Test
        fun `can access other owner's public collections`() {
            val query = collectionSearchQueryAssembler(
                public = true,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `can access any private collections`() {
            val query = collectionSearchQueryAssembler(
                public = false,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = privateOnly())
            )
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = CollectionVisibilityQuery.All)
            )
        }

        @Test
        fun `gets all private collections for no owner and private only`() {
            val query = collectionSearchQueryAssembler(
                public = false,
                owner = null,
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = privateOnly())
            )
        }

        @Test
        fun `gets all public collections if no visibility and owner specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = null,
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }
    }

    @Nested
    inner class `visibility for specific IDs` {
        @Test
        fun `can access other owner's public collections`() {
            val query = collectionSearchQueryAssembler(
                public = true,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.specificIds() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = publicOnly())
            )
        }

        @Test
        fun `can access any private collections`() {
            val query = collectionSearchQueryAssembler(
                public = false,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.specificIds() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = privateOnly())
            )
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.specificIds() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = "other-folk", visibility = CollectionVisibilityQuery.All)
            )
        }

        @Test
        fun `gets all public collections if no visibility and owner specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = null,
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.specificIds() })
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }
    }

    @Nested
    inner class `default values` {
        @Test
        fun `uses sane defaults for paging information and projections`() {
            val query = collectionSearchQueryAssembler()

            assertThat(query.pageIndex).isEqualTo(0)
            assertThat(query.pageSize).isEqualTo(CollectionsController.COLLECTIONS_PAGE_SIZE)
            assertThat(query.hasLessonPlans).isNull()
        }

        @Test
        fun `uses sane defaults for search related parameters`() {
            val query = collectionSearchQueryAssembler()

            assertThat(query.text).isEmpty()
            assertThat(query.subjectIds).isEmpty()
        }

        @Test
        fun `with superuser access, default to no visibility constraints`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }

        @Test
        fun `with owner access, default to all public and private owned collections`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly()),
                VisibilityForOwner(owner = "me", visibility = privateOnly())
            )
        }

        @Test
        fun `with specific ID access, default to no visibility constraints`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = {
                    AccessRulesFactory.specificIds(
                        CollectionId("blah")
                    )
                })
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }

        @Test
        fun `with public access, default to all public collections`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly())
            )
        }

        @Test
        fun `with owner access, take bookmarkedBy from access rule`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(
                    id = "authenticated",
                    accessRulesSupplier = { AccessRulesFactory.asOwner(ownerId = "access") }),
                bookmarked = true
            )

            assertThat(query.bookmarkedBy).isEqualTo("access")
        }

        @Test
        fun `with superuser access, take bookmarkedBy from passed-in user`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(
                    id = "authenticated",
                    accessRulesSupplier = { AccessRulesFactory.superuser() }),
                bookmarked = true
            )

            assertThat(query.bookmarkedBy).isEqualTo("authenticated")
        }

        @Test
        fun `with specific ID access, throw error when requesting bookmarked collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    bookmarked = true,
                    user = UserFactory.sample(accessRulesSupplier = {
                        AccessRulesFactory.specificIds()
                    })
                )
            }
        }

        @Test
        fun `with public access, throw error when requesting bookmarked collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    bookmarked = true,
                    user = UserFactory.sample(accessRulesSupplier = {
                        AccessRules(
                            videoAccess = VideoAccessRule.Everything,
                            collectionAccess = CollectionAccessRule.public()
                        )
                    })
                )
            }
        }
    }

    private fun collectionSearchQueryAssembler(
        query: String? = null,
        subjects: List<String> = emptyList(),
        public: Boolean? = null,
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null,
        hasLessonPlans: Boolean? = null,
        user: User = UserFactory.sample(accessRulesSupplier = {
            AccessRules(
                videoAccess = VideoAccessRule.Everything,
                collectionAccess = CollectionAccessRule.public()
            )
        })
    ) = CollectionSearchQueryAssembler()(
        query,
        subjects,
        public,
        bookmarked,
        owner,
        page,
        size,
        hasLessonPlans,
        user
    )
}
