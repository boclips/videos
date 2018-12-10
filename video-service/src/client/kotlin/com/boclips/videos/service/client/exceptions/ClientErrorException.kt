package com.boclips.videos.service.client.exceptions

class ClientErrorException(url: String, statusCode: Int) : RuntimeException("$url $statusCode")
