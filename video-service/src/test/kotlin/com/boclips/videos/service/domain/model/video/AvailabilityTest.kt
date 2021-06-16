package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AvailabilityTest {
    @Test
    fun `is downloadable`() {
        assertThat(ContentPartnerAvailability.DOWNLOAD.isDownloadable()).isTrue()
        assertThat(ContentPartnerAvailability.ALL.isDownloadable()).isTrue()
        assertThat(ContentPartnerAvailability.STREAMING.isDownloadable()).isFalse()
        assertThat(ContentPartnerAvailability.NONE.isDownloadable()).isFalse()
    }

    @Test
    fun `is streamable`() {
        assertThat(ContentPartnerAvailability.STREAMING.isStreamable()).isTrue()
        assertThat(ContentPartnerAvailability.ALL.isStreamable()).isTrue()

        assertThat(ContentPartnerAvailability.DOWNLOAD.isStreamable()).isFalse()
        assertThat(ContentPartnerAvailability.NONE.isStreamable()).isFalse()
    }
}
