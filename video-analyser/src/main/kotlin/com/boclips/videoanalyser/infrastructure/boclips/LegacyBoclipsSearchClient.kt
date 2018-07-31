package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.search_benchmark.service.SearchClient
import com.jayway.jsonpath.JsonPath
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class LegacyBoclipsSearchClient(
        restTemplateBuilder: RestTemplateBuilder,
        legacySearchProperties: LegacySearchProperties
) : SearchClient {

    val restTemplate = legacySearchProperties.validate().let {
        restTemplateBuilder
                .rootUri(legacySearchProperties.baseUrl)
                .build()!!
    }

    val token = legacySearchProperties.token

    override fun searchTop10(query: String): Iterable<String> {

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $token")

        val entity = HttpEntity<Any>(headers)

        val output = restTemplate.exchange("/search?q=_text_:($query)", HttpMethod.GET, entity, String::class.java)
        val ids : List<String> = JsonPath.read(output.body, "$.response.docs[*].id")
        return ids.take(10)
    }
}
