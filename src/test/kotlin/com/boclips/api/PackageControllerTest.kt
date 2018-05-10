package com.boclips.api

import org.junit.Test

class PackageControllerTest : AbstractIntegrationTest() {

    @Test
    fun getPackages_returnsPackages() {
        webClient.get().uri("/packages").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.packages[0].name").isEqualTo("pearson")
                .returnResult()

    }
}

