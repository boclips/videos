package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.service.SearchClient
import com.jayway.jsonpath.JsonPath
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component

@Component
class LegacyBoclipsSearchClient(restTemplateBuilder: RestTemplateBuilder, legacySearchProperties: LegacySearchProperties) : SearchClient {

    val restTemplate = restTemplateBuilder
            .rootUri(legacySearchProperties.baseUrl)
            .build()!!

    override fun searchTop10(query: String): Iterable<String> {
        val output = restTemplate.getForObject("/search?q=_text_:($query)", String::class.java)
        val ids : List<String> = JsonPath.read(output, "$.response.docs[*].id")
        return ids.take(10)
    }
}