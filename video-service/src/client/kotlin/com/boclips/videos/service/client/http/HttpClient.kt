package com.boclips.videos.service.client.http

import com.boclips.videos.service.client.exceptions.ClientErrorException
import com.boclips.videos.service.client.exceptions.ResourceForbiddenException
import com.boclips.videos.service.client.exceptions.ResourceNotFoundException
import com.boclips.videos.service.client.exceptions.ServerErrorException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

object HttpClient {

    fun makeRequest(request: Request): Response {
        val response = request.response().second
        when {
            response.statusCode == 403 -> throw ResourceForbiddenException(request.url.toString())
            response.statusCode == 404 -> throw ResourceNotFoundException(request.url.toString())
            response.statusCode >= 500 -> throw ServerErrorException(request.url.toString())
            response.statusCode >= 400 -> throw ClientErrorException(request.url.toString(), response.statusCode)
        }
        return response
    }
}
