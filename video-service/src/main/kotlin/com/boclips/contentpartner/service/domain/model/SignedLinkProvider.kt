package com.boclips.contentpartner.service.domain.model

import java.net.URL

interface SignedLinkProvider {
    fun signedPutLink(filename: String): URL
    fun signedGetLink(link: URL): URL
}
