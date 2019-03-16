package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BuildLegacySearchIndexTest {

    lateinit var legacySearchService: LegacySearchService

    @BeforeEach
    internal fun setUp() {
        legacySearchService = mock()
    }

    @Test
    fun `execute builds search index`() {
        val videoAssetId1 = TestFactories.aValidId()
        val videoAssetId2 = TestFactories.aValidId()

        val videoAssetRepository = mockVideoAssetRepository(
            videos = sequenceOf(
                TestFactories.createVideoAsset(
                    videoId = videoAssetId1,
                    title = "a title",
                    keywords = listOf("keyword")
                ),
                TestFactories.createVideoAsset(videoId = videoAssetId2, keywords = listOf("keyword"))
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoAssetRepository, legacySearchService)

        assertThat(rebuildSearchIndex()).isCompleted.hasNotFailed()

        val videos = getUpsertedVideos()
        assertThat(videos).hasSize(2)
        assertThat(videos.first().id).isEqualTo(videoAssetId1)
        assertThat(videos.first().title).isEqualTo("a title")
    }

    @Test
    fun `execute ignores videos with an empty title`() {
        val videoAssetRepository = mockVideoAssetRepository(
            videos = sequenceOf(
                TestFactories.createVideoAsset(
                    title = ""
                )
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoAssetRepository, legacySearchService)

        rebuildSearchIndex()

        assertThat(getUpsertedVideos()).isEmpty()
    }

    @Test
    fun `execute ignores youtube videos`() {
        val videoAssetRepository = mockVideoAssetRepository(
            videos = sequenceOf(
                TestFactories.createVideoAsset(
                    playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, "video")
                )
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoAssetRepository, legacySearchService)

        rebuildSearchIndex()

        assertThat(getUpsertedVideos()).isEmpty()
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAllSearchable(any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex = BuildLegacySearchIndex(videoAssetRepository, legacySearchService)

        assertThat(rebuildSearchIndex()).hasFailedWithThrowableThat().hasMessage("Boom")
    }

    private fun mockVideoAssetRepository(videos: Sequence<VideoAsset>): VideoAssetRepository {
        return mock {
            on {
                streamAllSearchable(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<VideoAsset>) -> Unit
                consumer(videos)
            }
        }
    }

    fun getUpsertedVideos(): List<LegacyVideoMetadata> {
        var upsertedVideos: List<LegacyVideoMetadata>? = null

        argumentCaptor<Sequence<LegacyVideoMetadata>>().apply {
            verify(legacySearchService).upsert(capture(), anyOrNull())

            upsertedVideos = firstValue.toList()
        }

        return upsertedVideos!!
    }
}