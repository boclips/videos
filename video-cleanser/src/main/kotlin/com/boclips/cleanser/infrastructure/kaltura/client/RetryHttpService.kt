package com.boclips.cleanser.infrastructure.kaltura.client

import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class RetryHttpService {
    companion object : KLogging()

    private val restTemplate = RestTemplate()

    @Retryable(value = [HttpRequestException::class], maxAttempts = 5)
    fun post(request: HttpEntity<MultiValueMap<String, String>>, uri: URI): MediaList {
        try {
            return restTemplate.postForEntity(
                    uri,
                    request,
                    MediaList::class.java).body!!
        } catch (ex: Throwable) {
            logger.warn("Request $uri failed, retrying...")
            throw HttpRequestException(ex)
        }
    }
}