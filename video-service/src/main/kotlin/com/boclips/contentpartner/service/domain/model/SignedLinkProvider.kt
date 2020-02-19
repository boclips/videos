package com.boclips.contentpartner.service.domain.model

import java.net.URL

interface SignedLinkProvider {
    fun getLink(): URL
}