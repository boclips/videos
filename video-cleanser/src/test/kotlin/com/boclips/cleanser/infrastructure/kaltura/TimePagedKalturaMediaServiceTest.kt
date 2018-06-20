package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaItem
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import com.nhaarman.mockito_kotlin.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class TimePagedKalturaMediaServiceTest {
    @Test
    fun handlesSmallDataSet() {
        val mockKalturaClient = Mockito.mock(KalturaMediaClient::class.java)
        Mockito.`when`(mockKalturaClient.count(emptyList())).thenReturn(2L)
        Mockito.`when`(mockKalturaClient.fetch(any(), any(), any()))
                .thenReturn(listOf(MediaItem(referenceId = "1"), MediaItem(referenceId = "2")))

        val mediaItems = TimePagedKalturaMediaService(mockKalturaClient).getReadyMediaEntries()

        assertThat(mediaItems).containsExactly(MediaItem(referenceId = "1"), MediaItem(referenceId = "2"))
    }
}