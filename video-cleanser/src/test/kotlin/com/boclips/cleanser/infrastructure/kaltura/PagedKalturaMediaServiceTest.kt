package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.domain.model.MediaItem
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class PagedKalturaMediaServiceTest {
    lateinit var pagedKalturaMediaService: PagedKalturaMediaService

    @Test
    fun getReadyMediaEntries_parsesMediaItems() {
        val mockKalturaClient = Mockito.mock(KalturaMediaClient::class.java)
        pagedKalturaMediaService = PagedKalturaMediaService(mockKalturaClient)
        Mockito.`when`(mockKalturaClient.fetch(500, 0))
                .thenReturn(listOf(MediaItem(referenceId = "1"), MediaItem(referenceId = "2")))

        val mediaItems = pagedKalturaMediaService.getReadyMediaEntries()

        assertThat(mediaItems).containsExactly(MediaItem(referenceId = "1"), MediaItem(referenceId = "2"))
    }

}