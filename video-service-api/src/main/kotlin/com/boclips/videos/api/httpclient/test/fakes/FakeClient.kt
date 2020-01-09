package com.boclips.videos.api.httpclient.test.fakes

interface FakeClient<T> {
    fun add(element: T): T
    fun findAll() : List<T>
    fun clear()
}
