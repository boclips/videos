package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BuildLegacySearchIndexTest {

    @Test
    fun `execute builds search index`() {
        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<VideoAsset>) -> Unit
                consumer(sequenceOf(
                        TestFactories.createVideoAsset(videoId = "1", title = "a title"),
                        TestFactories.createVideoAsset(videoId = "2")
                ))
            }
        }
        val legacySearchService = mock<LegacySearchService>()

        val rebuildSearchIndex = BuildLegacySearchIndex(videoAssetRepository, legacySearchService)

        rebuildSearchIndex.execute()

        argumentCaptor<Sequence<LegacyVideoMetadata>>().apply {
            verify(legacySearchService).upsert(capture())

            val videos = firstValue.toList()
            assertThat(videos).hasSize(2)
            assertThat(videos.first().id).isEqualTo("1")
            assertThat(videos.first().title).isEqualTo("a title")

        }
    }
}