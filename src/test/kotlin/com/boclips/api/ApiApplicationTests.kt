package com.boclips.api

import com.mongodb.client.result.DeleteResult
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.net.ConnectException

class ApiApplicationTests : AbstractIntegrationTest() {

    private val NON_SNTV_VIDEO = 2439228

    @Autowired
    lateinit var videos: VideoRepository

    @SpyBean
    lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Test
    fun deleteProvider_deletesVideosByProvider() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.videosRemoved").isEqualTo(1)


        assertThat(videos.findAll().map { it.source }).containsExactly("Grinberg, Paramount, Pathe Newsreels")
    }

    @Test
    fun deleteProvider_deletesVideosFromPlaylistsByProviderReturningRecordsDeleted() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.playlistEntriesRemoved").isEqualTo(1)


        val videos = mongoTemplate.findAll(Document::class.java, "videodescriptors")
        assertThat(videos.map { it["reference_id"] }).containsExactly(NON_SNTV_VIDEO)
    }

    @Test
    fun deleteProvider_deletesVideosFromCartsByProvider() {
        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.orderlinesEntriesRemoved").isEqualTo(1)


        val videos = mongoTemplate.findAll(Document::class.java, "orderlines")
        assertThat(videos.map { it["asset_id"] }).containsExactly(NON_SNTV_VIDEO)
    }

    @Test
    fun deleteProvider_whenSomeMongoQueryFails_returnsError() {
        doReturn(DeleteResult.acknowledged(1).toMono()).whenever(reactiveMongoTemplate).remove(any<Query>(), any<String>())
        doReturn(Mono.error<Exception>(ConnectException())).whenever(reactiveMongoTemplate).remove(any<Query>(), any<String>())

        webClient.delete().uri("/content-providers/SNTV").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.videosRemoved").isEqualTo(0)

        assertThat(videos.findAll()).hasSize(2)
    }


}
