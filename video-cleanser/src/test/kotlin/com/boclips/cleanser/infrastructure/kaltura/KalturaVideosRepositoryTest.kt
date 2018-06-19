package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.infrastructure.kaltura.response.MediaItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class KalturaVideosRepositoryTest {
    lateinit var kalturaVideosRepository: KalturaVideosRepository

    @Test
    fun getAllNonErroredVideos_parsesMediaItems() {
        val mockKalturaClient = Mockito.mock(KalturaMediaClient::class.java)
        kalturaVideosRepository = KalturaVideosRepository(mockKalturaClient)
        Mockito.`when`(mockKalturaClient.fetch(500, 0))
                .thenReturn(listOf(MediaItem(referenceId = "1"), MediaItem(referenceId = "2")))

        val allNonErroredVideoIds = kalturaVideosRepository.getAllIds()

        assertThat(allNonErroredVideoIds).containsExactly("1", "2")
    }
}