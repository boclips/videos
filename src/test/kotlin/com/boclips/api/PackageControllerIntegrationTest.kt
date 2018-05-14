package com.boclips.api

import com.boclips.api.testsupport.PEARSON_PACKAGE_ID
import com.boclips.api.testsupport.SKY_NEWS_ID
import com.boclips.api.testsupport.SKY_NEWS_NAME
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class PackageControllerIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun getPackages_returnsPackages() {
        webClient.get().uri("/packages").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.packages[0].name").exists()
                .jsonPath("$._embedded.packages[0].excludedContentProviders[0]").exists()
                .jsonPath("$._embedded.packages[0]._links.self.href").exists()
    }

    @Test
    fun getPackages_whenRestrictingContentProvider_returnsLinkToAddContentProviderFilter() {
        webClient.get().uri("/packages").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.packages[0]._links.excludedContentProvider.href").isNotEmpty
    }

    @Test
    fun getPackage() {
        webClient.get().uri("/packages/$PEARSON_PACKAGE_ID").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.name").exists()
                .jsonPath("$.excludedContentProviders[0]").exists()
                .jsonPath("$._links.self.href").exists()
    }

    @Ignore("WIP")
    @Test
    fun excludeContentProvider_whenPreviouslyIncluded() {
        val skyNewsUrl = webClient
                .get().uri("/content-providers/${SKY_NEWS_ID}").exchange()
                .getJsonPathAsString("$._links.self.href")

        val pearsonPackage = webClient
                .get().uri("/packages/${PEARSON_PACKAGE_ID}").exchange()
                .getBodyAsString()
        val excludedContentProviderUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.excludedContentProvider.href")
        val packageUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.self.href")
        assertThat(webClient.get().uri(packageUrl).exchange()
                .getJsonPathAsString("$.excludedContentProviders[0].name")).isEqualTo(SKY_NEWS_NAME)

        webClient
                .patch().uri(excludedContentProviderUrl).contentType(MediaType("text", "uri-list")).syncBody(skyNewsUrl).exchange()
                .expectStatus().isNoContent

        assertThat(webClient.get().uri(packageUrl).exchange()
                .getJsonPathAsString("$.contentProviders[?(@.id='${SKY_NEWS_ID}')].excluded")).isEqualTo(true)
    }


}

fun WebTestClient.ResponseSpec.getBodyAsString() = this.expectStatus().is2xxSuccessful().returnResult(String::class.java).responseBody.blockFirst()
fun WebTestClient.ResponseSpec.getJsonPathAsString(jsonPath: String) = JsonPath.parse(this.getBodyAsString()).read<String>(jsonPath)
