package com.boclips.videos.service.presentation

open class ApiException : RuntimeException()
class ResourceNotFoundException : ApiException()
class IllegalFilterException : ApiException()
