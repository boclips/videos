package com.boclips.videos.presentation

open class ApiException : RuntimeException()
class ResourceNotFoundException : ApiException()
class IllegalFilterException : ApiException()
