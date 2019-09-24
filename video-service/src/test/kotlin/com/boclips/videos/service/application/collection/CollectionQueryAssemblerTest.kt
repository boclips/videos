package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.CollectionFilterFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
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
        assertDoesNotThrow {
            val result = CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.BOOKMARKED
                ),
                user = UserFactory.sample(id = "alex")
            )
            assertThat(result.bookmarkedBy).isEqualTo("alex")
            assertThat(result.visibility).containsOnly(CollectionVisibility.PUBLIC)
        }
    }

}
