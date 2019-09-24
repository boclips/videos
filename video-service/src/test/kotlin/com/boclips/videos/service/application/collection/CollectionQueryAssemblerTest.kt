package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.CollectionAccessRule
import com.boclips.videos.service.testsupport.CollectionFilterFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class CollectionQueryAssemblerTest {
    @Test
    fun `user without special role cannot retrieve private collections`() {
        assertThrows<OperationForbiddenException> {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.PRIVATE
                ),
                user = UserFactory.sample()
            )
        }
    }

    @Test
    fun `user without special role cannot retrieve all collections`() {
        assertThrows<OperationForbiddenException> {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.ALL
                ),
                user = UserFactory.sample()
            )
        }
    }

    @Test
    fun `user with special role can retrieve all collections`() {
        assertDoesNotThrow {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.ALL
                ),
                user = UserFactory.sample(roles = setOf(UserRoles.VIEW_ANY_COLLECTION))
            )
        }
    }

    @Test
    fun `user cannot get private collections belonging to others`() {
        assertThrows<OperationForbiddenException> {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.PRIVATE,
                    owner = "alex"
                ),
                user = UserFactory.sample(id = "antony")
            )
        }
    }

    @Test
    fun `user can get private collections belonging to themselves`() {
        assertDoesNotThrow {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.PRIVATE,
                    owner = "alex"
                ),
                user = UserFactory.sample(id = "alex")
            )
        }
    }

    @Test
    fun `user can get bookmarked collections`() {
        val result = CollectionQueryAssembler().assemble(
            filter = CollectionFilterFactory.sample(
                visibility = CollectionFilter.Visibility.BOOKMARKED
            ),
            user = UserFactory.sample(id = "alex")
        )
        assertThat(result.bookmarkedBy).isEqualTo("alex")
        assertThat(result.visibility).containsOnly(CollectionVisibility.PUBLIC)
    }

    @Nested
    inner class Contracts {
        @Test
        fun `user is only permitted to access to some collections`() {
            val result = CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.BOOKMARKED
                ),
                user = UserFactory.sample(id = "alex-123"),
                collectionAccess = CollectionAccessRule.RestrictedTo(setOf(CollectionId("1"), CollectionId("2")))
            )
            assertThat(result.permittedCollections).containsExactlyInAnyOrder(CollectionId("1"), CollectionId("2"))
        }


        @Test
        fun `user is permitted all collections`() {
            val result = CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.BOOKMARKED
                ),
                user = UserFactory.sample(id = "alex-123"),
                collectionAccess = CollectionAccessRule.All
            )
            assertThat(result.permittedCollections).isNull()
        }
    }
}
