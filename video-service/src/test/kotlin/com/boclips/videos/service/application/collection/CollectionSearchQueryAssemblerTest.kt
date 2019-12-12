package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery.Companion.privateOnly
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery.Companion.publicOnly
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.CollectionId
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
            accessRules = AccessRulesFactory.asOwner("my-id"),
            owner = "other-id"
        )

        assertThat(query.text).isEqualTo("minute physics")
        assertThat(query.subjectIds).containsOnly("Physics")
        assertThat(query.visibilityForOwners).containsExactly(VisibilityForOwner("other-id", publicOnly()))
        assertThat(query.bookmarkedBy).isEqualTo("my-id")
        assertThat(query.pageIndex).isEqualTo(1)
        assertThat(query.pageSize).isEqualTo(30)
    }

    @Test
    fun `assembles a query with specific ID access`() {
        val collectionIds = arrayOf(CollectionId("1"), CollectionId("2"))

        val query = collectionSearchQueryAssembler(
            accessRules = AccessRulesFactory.specificIds(*collectionIds)
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
                accessRules = AccessRulesFactory.asOwner("me")
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
                    accessRules = AccessRulesFactory.asOwner("me")
                )
            }
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                accessRules = AccessRulesFactory.asOwner("me")
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
                accessRules = AccessRulesFactory.asOwner("me")
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
                accessRules = AccessRulesFactory.publicOnly()
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
                    accessRules = AccessRulesFactory.publicOnly()
                )
            }
        }

        @Test
        fun `gets only other owner's public collections if no visibility specified`() {
            val query = collectionSearchQueryAssembler(
                public = null,
                owner = "other-folk",
                accessRules = AccessRulesFactory.publicOnly()
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
                accessRules = AccessRulesFactory.publicOnly()
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
                accessRules = AccessRulesFactory.superuser()
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
                accessRules = AccessRulesFactory.superuser()
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
                accessRules = AccessRulesFactory.superuser()
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
                accessRules = AccessRulesFactory.superuser()
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
                accessRules = AccessRulesFactory.superuser()
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
                accessRules = AccessRulesFactory.specificIds()
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
                accessRules = AccessRulesFactory.specificIds()
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
                accessRules = AccessRulesFactory.specificIds()
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
                accessRules = AccessRulesFactory.specificIds()
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
                accessRules = AccessRulesFactory.superuser()
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }

        @Test
        fun `with owner access, default to all public and private owned collections`() {
            val query = collectionSearchQueryAssembler(
                accessRules = AccessRulesFactory.asOwner("me")
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly()),
                VisibilityForOwner(owner = "me", visibility = privateOnly())
            )
        }

        @Test
        fun `with specific ID access, default to no visibility constraints`() {
            val query = collectionSearchQueryAssembler(
                accessRules = AccessRulesFactory.specificIds(
                    CollectionId("blah")
                )
            )

            assertThat(query.visibilityForOwners).isEmpty()
        }

        @Test
        fun `with public access, default to all public collections`() {
            val query = collectionSearchQueryAssembler(
                accessRules = AccessRulesFactory.publicOnly()
            )

            assertThat(query.visibilityForOwners).containsExactlyInAnyOrder(
                VisibilityForOwner(owner = null, visibility = publicOnly())
            )
        }

        @Test
        fun `with owner access, take bookmarkedBy from access rule`() {
            val query = collectionSearchQueryAssembler(
                accessRules = AccessRulesFactory.asOwner(ownerId = "access"),
                user = UserFactory.sample(id = "authenticated"),
                bookmarked = true
            )

            assertThat(query.bookmarkedBy).isEqualTo("access")
        }

        @Test
        fun `with superuser access, take bookmarkedBy from passed-in user`() {
            val query = collectionSearchQueryAssembler(
                accessRules = AccessRulesFactory.superuser(),
                user = UserFactory.sample(id = "authenticated"),
                bookmarked = true
            )

            assertThat(query.bookmarkedBy).isEqualTo("authenticated")
        }

        @Test
        fun `with specific ID access, throw error when requesting bookmarked collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    accessRules = AccessRulesFactory.specificIds(),
                    bookmarked = true
                )
            }
        }

        @Test
        fun `with public access, throw error when requesting bookmarked collections`() {
            assertThrows<OperationForbiddenException> {
                collectionSearchQueryAssembler(
                    accessRules = AccessRulesFactory.publicOnly(),
                    bookmarked = true
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
        accessRules: AccessRules = AccessRulesFactory.publicOnly(),
        user: User? = null
    ) = CollectionSearchQueryAssembler()(
        query,
        subjects,
        public,
        bookmarked,
        owner,
        page,
        size,
        accessRules,
        user
    )
}
