package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.contentpartner.GetContentPartner
import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetContentPartnerTest {

    @Test
    fun `when content partner not found throws`() {
        assertThrows<ResourceNotFoundApiException> {
            GetContentPartner(mock()).invoke("doesn't exist id")
        }
    }
}
