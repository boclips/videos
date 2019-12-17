package com.boclips.videos.service.domain.model.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AvailabilityTest {
    @Test
    fun `is downloadable`() {
        assertThat(Availability.DOWNLOAD.isDownloadable()).isTrue()
        assertThat(Availability.ALL.isDownloadable()).isTrue()
        assertThat(Availability.STREAMING.isDownloadable()).isFalse()
        assertThat(Availability.NONE.isDownloadable()).isFalse()
    }

    @Test
    fun `is streamable`() {
        assertThat(Availability.STREAMING.isStreamable()).isTrue()
        assertThat(Availability.ALL.isStreamable()).isTrue()

        assertThat(Availability.DOWNLOAD.isStreamable()).isFalse()
        assertThat(Availability.NONE.isStreamable()).isFalse()
    }
}
