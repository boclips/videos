package com.boclips.videos.api.httpclient.helper

interface TokenFactory {
    fun getAccessToken() : String
}
