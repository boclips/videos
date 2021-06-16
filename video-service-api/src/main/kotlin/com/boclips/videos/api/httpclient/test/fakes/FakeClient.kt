package com.boclips.videos.api.httpclient.test.fakes

import feign.FeignException
import feign.Request
import feign.RequestTemplate

interface FakeClient<T> {
    fun add(element: T): T
    fun findAll(): List<T>
    fun clear()

    companion object {
        @JvmStatic
        fun notFoundException(message: String) = FeignException.NotFound(
            message,
            Request.create(
                Request.HttpMethod.GET,
                "http://this.com/does/not/exist",
                emptyMap(),
                Request.Body.empty(),
                RequestTemplate()
            ),
            null
        )

        @JvmStatic
        fun conflictException(message: String) = FeignException.Conflict(
            message,
            Request.create(
                Request.HttpMethod.PUT,
                "http://this.com/does/exist",
                emptyMap(),
                Request.Body.empty(),
                RequestTemplate()
            ),
            null
        )
    }
}
