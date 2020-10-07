package com.boclips.videos.api.httpclient.helper

import com.boclips.security.testing.FakeTokenHelper

class TestTokenFactory(
    private val username: String = "the@owner.com",
    private vararg val roles: String = arrayOf("ROLE_WINNER")
) : TokenFactory {
    override fun getAccessToken(): String {
        return FakeTokenHelper().createToken(username = username, roles = roles)
    }
}
