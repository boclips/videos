package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import java.net.URI
import java.net.URL

class TestSignedLinkProvider : SignedLinkProvider {
    private val defaultLink = URI("https://default.com/").toURL()
    private var signedLink = defaultLink

    fun setLink(link: URL) {
        signedLink = link
    }

    override fun getLink(): URL = signedLink
    fun clearLink() {
        signedLink = defaultLink
    }
}