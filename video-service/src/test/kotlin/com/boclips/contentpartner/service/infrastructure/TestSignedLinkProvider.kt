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

    override fun signedPutLink(filename: String): URL = signedLink
    override fun signedGetLink(link: URL): URL = signedLink

    fun clearLink() {
        signedLink = defaultLink
    }
}
