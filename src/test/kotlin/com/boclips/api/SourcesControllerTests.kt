package com.boclips.api

import org.junit.Test

class SourcesControllerTests : AbstractIntegrationTest() {

    @Test
    fun getSources_returnsSources() {
        webClient.get().uri("/sources").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("59172cbce5a04e5e69d1ba30")
                .jsonPath("$[0].name").isEqualTo("Sky News")
                .jsonPath("$[0].dateCreated").isEqualTo("2017-05-13T15:56:44.822Z")
                .jsonPath("$[0].dateUpdated").isEqualTo("2017-05-13T15:56:44.822Z")
                .jsonPath("$[0].uuid").isEqualTo("86360cc5-019e-4b13-9048-1d571e825108")
    }

    @Test
    fun putSource_whenItDoesNotExist_createsNewSource() {

        webClient.put().uri("/sources/TeD").exchange()
                .expectStatus().isCreated

        webClient.get().uri("/sources").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$[1].id").isNotEmpty
                .jsonPath("$[1].name").isEqualTo("TeD")
                .jsonPath("$[1].dateCreated").isNotEmpty
                .jsonPath("$[1].dateUpdated").isNotEmpty
    }

    @Test
    fun putSource_whenItExists_doesNotCreateANewSource() {

        webClient.put().uri("/sources/Sky News").exchange()
                .expectStatus().isOk

        webClient.get().uri("/sources").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$[1]").doesNotExist()
    }
}

