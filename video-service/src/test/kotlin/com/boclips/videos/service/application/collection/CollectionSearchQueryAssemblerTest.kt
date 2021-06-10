package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoAccess
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
            owner = "other-id",
            page = 1,
            size = 30,
            sort = listOf(CollectionSortKey.TITLE),
            hasLessonPlans = true,
            user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("my-id") }),
            ageRangeMin = 3,
            ageRangeMax = 7,
            ageRange = listOf("3-7")
        )

        assertThat(query.text).isEqualTo("minute physics")
        assertThat(query.subjectIds).containsOnly("Physics")
        assertThat(query.discoverable).isEqualTo(null)
        assertThat(query.owner).isEqualTo("other-id")
        assertThat(query.bookmarkedBy).isEqualTo("my-id")
        assertThat(query.pageIndex).isEqualTo(1)
        assertThat(query.pageSize).isEqualTo(30)
        assertThat(query.sort).containsExactly(CollectionSortKey.TITLE)
        assertThat(query.hasLessonPlans).isTrue()
        assertThat(query.ageRangeMin).isEqualTo(3)
        assertThat(query.ageRangeMax).isEqualTo(7)
        assertThat(query.ageRanges).isEqualTo(listOf(AgeRange.of(min = 3, max = 7, curatedManually = false)))
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
                owner = "other-folk",
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )
            assertThat(query.owner).isEqualTo("other-folk")
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
        fun `with owner access, default to all public and private owned collections`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.owner).isEqualTo("me")
            assertThat(query.discoverable).isNull()
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

            assertThat(query.discoverable).isNull()
            assertThat(query.permittedCollections).containsExactly(CollectionId("blah"))
        }

        @Test
        fun `with owner access, take bookmarkedBy from access rule`() {
            val query = collectionSearchQueryAssembler(
                bookmarked = true,
                user = UserFactory.sample(
                    id = "authenticated",
                    accessRulesSupplier = { AccessRulesFactory.asOwner(ownerId = "access") })
            )

            assertThat(query.bookmarkedBy).isEqualTo("access")
        }

        @Test
        fun `with access to everything, take bookmarkedBy from passed-in user`() {
            val query = collectionSearchQueryAssembler(
                bookmarked = true,
                user = UserFactory.sample(
                    id = "authenticated",
                    accessRulesSupplier = { AccessRulesFactory.everything() })
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
    }

    @Test
    fun `when access to everything respects discoverable flag`() {
        val query = collectionSearchQueryAssembler(
            discoverable = false,
            user = UserFactory.sample(accessRulesSupplier = {
                AccessRulesFactory.everything()
            })
        )

        assertThat(query.discoverable).isFalse()
    }

    @Test
    fun `when elaborate access rules are in place defaults discoverable to null`() {
        val query = collectionSearchQueryAssembler(
            discoverable = false,
            user = UserFactory.sample(accessRulesSupplier = {
                AccessRulesFactory.specificIds()
            })
        )

        assertThat(query.discoverable).isNull()
    }

    private fun collectionSearchQueryAssembler(
        query: String? = null,
        subjects: List<String> = emptyList(),
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null,
        sort: List<CollectionSortKey> = emptyList(),
        hasLessonPlans: Boolean? = null,
        user: User = UserFactory.sample(accessRulesSupplier = {
            AccessRules(
                videoAccess = VideoAccess.Everything(emptySet()),
                collectionAccess = CollectionAccessRule.everything()
            )
        }),
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        ageRange: List<String>? = null,
        discoverable: Boolean = true
    ) = CollectionSearchQueryAssembler()(
        query = query,
        subjects = subjects,
        bookmarked = bookmarked,
        discoverable = discoverable,
        owner = owner,
        page = page,
        size = size,
        sort = sort,
        hasLessonPlans = hasLessonPlans,
        user = user,
        ageRangeMin = ageRangeMin,
        ageRangeMax = ageRangeMax,
        ageRange = ageRange
    )
}
