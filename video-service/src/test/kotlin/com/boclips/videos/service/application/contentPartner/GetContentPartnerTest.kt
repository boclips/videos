package com.boclips.videos.service.application.contentPartner

import com.boclips.web.exceptions.ResourceNotFoundApiException
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetContentPartnerTest {

    @Test
    fun `when content partner not found throws`() {
        assertThrows<ResourceNotFoundApiException> { GetContentPartner(mock()).invoke("doesn't exist id") }
    }
}