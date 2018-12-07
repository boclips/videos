package com.boclips.videos.service.client.http

import com.boclips.videos.service.client.exceptions.ResourceForbiddenException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

object HttpClient {

    fun makeRequest(request: Request): Response {
        val response = request.response().second
        when (response.statusCode) {
            403 -> throw ResourceForbiddenException(request.url.toString())
        }
        return response
    }
}
