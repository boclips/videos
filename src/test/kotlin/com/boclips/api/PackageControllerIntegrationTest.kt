package com.boclips.api

import com.boclips.api.testsupport.PEARSON_PACKAGE_ID
import com.boclips.api.testsupport.SKY_NEWS_ID
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class PackageControllerTest : AbstractIntegrationTest() {

    @Test
    fun getPackages_returnsPackages() {
        webClient.get().uri("/packages").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.packages[0].name").isEqualTo("pearson")
                .returnResult()
    }

    @Test
    fun getPackages_whenRestrictingContentProvider_returnsLinkToAddContentProviderFilter() {
        webClient.get().uri("/packages").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.packages[0]._links.excludeContentProvider.href").isNotEmpty
    }


    @Ignore("work in progress")
    @Test
    fun excludeContentProvider_whenPreviouslyIncluded() {
        val skyNewsUrl = webClient
                .get().uri("/content-providers/${SKY_NEWS_ID}").exchange()
                .getJsonPathAsString("$._links.self.href")

        val pearsonPackage = webClient
                .get().uri("/packages/${PEARSON_PACKAGE_ID}").exchange()
                .getBodyAsString()
        val excludeContentProviderUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.excludeContentProvider.href")
        val packageUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.self.href")
        assertThat(webClient.get().uri(packageUrl).exchange()
                .getJsonPathAsString("$.contentProviders[?(@.id='${SKY_NEWS_ID}')].excluded")).isEqualTo(false)

        webClient
                .patch().uri(excludeContentProviderUrl).contentType(MediaType("text", "uri-list")).syncBody(skyNewsUrl).exchange()
                .expectStatus().isNoContent

        assertThat(webClient.get().uri(packageUrl).exchange()
                .getJsonPathAsString("$.contentProviders[?(@.id='${SKY_NEWS_ID}')].excluded")).isEqualTo(true)
    }


}

fun WebTestClient.ResponseSpec.getBodyAsString() = this.expectStatus().is2xxSuccessful().returnResult(String::class.java).responseBody.blockFirst()
fun WebTestClient.ResponseSpec.getJsonPathAsString(jsonPath: String) = JsonPath.parse(this.getBodyAsString()).read<String>(jsonPath)
