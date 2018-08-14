package com.boclips.videos

import com.boclips.videos.testsupport.AbstractIntegrationTest
import com.boclips.videos.testsupport.PEARSON_PACKAGE_ID
import com.boclips.videos.testsupport.SCHOOL_OF_LIFE_ID
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun excludeContentProvider_whenPreviouslyIncluded() {
        val schoolOfLifeUrl = webClient
                .get().uri("/content-providers/${SCHOOL_OF_LIFE_ID}").exchange()
                .getJsonPathAsString("$._links.self.href")

        val pearsonPackage = webClient
                .get().uri("/packages/${PEARSON_PACKAGE_ID}").exchange()
                .getBodyAsString()
        val excludedContentProviderUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.excludedContentProvider.href")
        val packageUrl = JsonPath.parse(pearsonPackage).read<String>("$._links.self.href")

        webClient
                .patch().uri(excludedContentProviderUrl).contentType(MediaType("text", "uri-list")).syncBody(schoolOfLifeUrl).exchange()
                .expectStatus().isNoContent

        assertThat(webClient.get().uri(packageUrl).exchange()
                .getJsonPathAsString("$.excludedContentProviders[1]._links.self.href")).isEqualTo(schoolOfLifeUrl)
    }
}

fun WebTestClient.ResponseSpec.getBodyAsString() = this.expectStatus().is2xxSuccessful().returnResult(String::class.java).responseBody.blockFirst()
fun WebTestClient.ResponseSpec.getJsonPathAsString(jsonPath: String) = JsonPath.parse(this.getBodyAsString()).read<String>(jsonPath)
