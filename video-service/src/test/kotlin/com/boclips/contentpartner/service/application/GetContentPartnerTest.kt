package com.boclips.contentpartner.service.application

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetContentPartnerTest {

    @Test
    fun `when content partner not found throws`() {
        val user = UserFactory.sample(roles = setOf(UserRoles.BACKOFFICE))
        assertThrows<ResourceNotFoundApiException> {
            GetContentPartner(
                mock()
            ).invoke("doesn't exist id", user)
        }
    }
}
