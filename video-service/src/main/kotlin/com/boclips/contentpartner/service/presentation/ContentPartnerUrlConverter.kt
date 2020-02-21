package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.exceptions.InvalidUrlException
import java.net.MalformedURLException
import java.net.URL

object ContentPartnerUrlConverter {
    fun convert(s: String): URL {
        try {
            return URL(s)
        } catch (e: MalformedURLException) {
            throw InvalidUrlException(s)
        }
    }
}
