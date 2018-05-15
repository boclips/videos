package com.boclips.api.presentation

open class ApiException : RuntimeException()
class ResourceNotFoundException : ApiException()
class IllegalFilterException : ApiException()
