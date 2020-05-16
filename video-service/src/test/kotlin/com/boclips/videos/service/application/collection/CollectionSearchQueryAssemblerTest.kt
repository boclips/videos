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
            page = 1,
            size = 30,
            sort = CollectionSortKey.TITLE,
            user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("my-id") }),
            owner = "other-id",
            hasLessonPlans = true,
            ageRangeMin = 3,
            ageRangeMax = 7,
            ageRange = listOf("3-7")
        )

        assertThat(query.text).isEqualTo("minute physics")
        assertThat(query.subjectIds).containsOnly("Physics")
        assertThat(query.curated).isEqualTo(null)
        assertThat(query.owner).isEqualTo("other-id")
        assertThat(query.bookmarkedBy).isEqualTo("my-id")
        assertThat(query.pageIndex).isEqualTo(1)
        assertThat(query.pageSize).isEqualTo(30)
        assertThat(query.sort).isEqualTo(CollectionSortKey.TITLE)
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
        fun `with superuser access, default to no visibility constraints`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.superuser() })
            )

            assertThat(query.curated).isNull()
        }

        @Test
        fun `with owner access, default to all public and private owned collections`() {
            val query = collectionSearchQueryAssembler(
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.asOwner("me") })
            )

            assertThat(query.owner).isEqualTo("me")
            assertThat(query.curated).isNull()
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

            assertThat(query.curated).isNull()
            assertThat(query.permittedCollections).containsExactly(CollectionId("blah"))
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
    }

    private fun collectionSearchQueryAssembler(
        query: String? = null,
        subjects: List<String> = emptyList(),
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null,
        sort: CollectionSortKey? = null,
        hasLessonPlans: Boolean? = null,
        user: User = UserFactory.sample(accessRulesSupplier = {
            AccessRules(
                videoAccess = VideoAccess.Everything,
                collectionAccess = CollectionAccessRule.everything()
            )
        }),
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        ageRange: List<String>? = null
    ) = CollectionSearchQueryAssembler()(
        query = query,
        subjects = subjects,
        bookmarked = bookmarked,
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
