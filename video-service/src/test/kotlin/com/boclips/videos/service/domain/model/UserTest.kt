package com.boclips.videos.service.domain.model

import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserTest {
    @Test
    fun `lazily evaluates access rules`(@Mock accessRulesSupplier: (user: User) -> AccessRules) {
        val user = UserFactory.sample(accessRulesSupplier = accessRulesSupplier)

        verify(accessRulesSupplier, never()).invoke(any())

        user.accessRules

        verify(accessRulesSupplier, times(1)).invoke(any())
    }
}
