package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.user.UserNotAuthenticatedException
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserTest {

    @Test
    fun `idOrThrow returns user id when present`() {
        val user = UserFactory.sample(id = "user-id")

        assertThat(user.idOrThrow()).isEqualTo(UserId("user-id"))
    }

    @Test
    fun `idOrThrow throws when no id is present`() {
        val user = UserFactory.sample(id = null)

        assertThrows<UserNotAuthenticatedException> {
            user.idOrThrow()
        }
    }

    @Test
    fun `lazily evaluates access rules`(@Mock accessRulesSupplier: (user: User) -> AccessRules) {
        val user = UserFactory.sample(accessRulesSupplier = accessRulesSupplier)

        verify(accessRulesSupplier, never()).invoke(any())

        user.accessRules

        verify(accessRulesSupplier, times(1)).invoke(any())
    }
}
