package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.CollectionFilterFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class CollectionQueryAssemblerTest {
    @Test
    fun `users without special role cannot retrieve private collections`() {
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
    fun `users without special role cannot retrieve all collections`() {
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
    fun `users with special role can retrieve all collections`() {
        assertDoesNotThrow {
            CollectionQueryAssembler().assemble(
                filter = CollectionFilterFactory.sample(
                    visibility = CollectionFilter.Visibility.ALL
                ),
                user = UserFactory.sample(roles = setOf(UserRoles.VIEW_ANY_COLLECTION))
            )
        }
    }
}
