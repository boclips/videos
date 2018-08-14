package com.boclips.videos.service

import com.boclips.videos.service.infrastructure.videos.VideoRepository
import com.boclips.videos.service.testsupport.AbstractIntegrationTest
import com.boclips.videos.service.testsupport.SKY_NEWS_ID
import org.assertj.core.api.Assertions
import org.bson.Document
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono

class ContentProviderControllerIntegrationTest : AbstractIntegrationTest() {

    private val NON_SNTV_VIDEO = 2439228

    @Autowired
    lateinit var videos: VideoRepository

    @Test
    fun getContentProviders_returnsContentProviders() {
        webClient.get().uri("/content-providers").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.contentProviders[0].name").isEqualTo("Sky News")
    }

    @Test
    fun getContentProviders_containsSelfLinks() {
        webClient.get().uri("/content-providers").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.contentProviders[0]._links.self").isNotEmpty
    }

    @Test
    fun postContentProvider_whenItDoesNotExist_createsNewContentProvider() {
        val body: Mono<Map<*, *>> = Mono.just(mapOf("name" to "TeD"))
        webClient.post().uri("/content-providers").body(body, Map::class.java).exchange()
                .expectStatus().isCreated

        webClient.get().uri("/content-providers").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.contentProviders[2].name").isEqualTo("TeD")
    }

    @Test
    fun postContentProvider_whenItExists_doesNotCreateANewContentProvider() {
        val body: Mono<Map<*, *>> = Mono.just(mapOf("name" to "Sky News"))
        webClient.post().uri("/content-providers").body(body, Map::class.java).exchange()
                .expectStatus().isOk

        webClient.get().uri("/content-providers").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$._embedded.contentProviders[0].name").isEqualTo("Sky News")
                .jsonPath("$._embedded.contentProviders[1].name").isEqualTo("The School of Life")
                .jsonPath("$._embedded.contentProviders[2]").doesNotExist()
    }

    @Test
    fun deleteContentProvider_deletesVideosByProvider() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.videosRemoved").isEqualTo(1)


        Assertions.assertThat(videos.findAll().map { it.source }).containsExactly("Grinberg, Paramount, Pathe Newsreels")
    }

    @Test
    fun deleteContentProvider_deletesVideosFromPlaylistsByProviderReturningRecordsDeleted() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.playlistEntriesRemoved").isEqualTo(1)


        val videos = mongoTemplate.findAll(Document::class.java, "videodescriptors")
        Assertions.assertThat(videos.map { it["reference_id"] }).containsExactly(NON_SNTV_VIDEO)
    }

    @Test
    fun deleteContentProvider_deletesVideosFromCartsByProvider() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.orderlinesEntriesRemoved").isEqualTo(1)


        val videos = mongoTemplate.findAll(Document::class.java, "orderlines")
        Assertions.assertThat(videos.map { it["asset_id"] }).containsExactly(NON_SNTV_VIDEO)
    }

    @Test
    fun getContentProvider() {
        webClient.get().uri("/content-providers/$SKY_NEWS_ID").exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.name").exists()
                .jsonPath("$._links.self.href").exists()
    }

    @Test
    fun getContentProvider_whenNoResource_returns404() {
        webClient.get().uri("/content-providers/non-existing-content-provider").exchange()
                .expectStatus().isNotFound
    }

}

