package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlaybackIdTest {

    @Test // A KALTURA_REFERENCE type playbackId with playbackId value, when provider is KALTURA, and kalturaEntryId is not provided
    fun `it should return a KALTURA_REFERENCE PlaybackId when KALTURA is provided and entryId is not provided`() {
        val request = TestFactories.createCreateVideoRequest(
            playbackProvider = PlaybackProviderType.KALTURA.name,
            playbackId = "ref-123"
        );

        val playbackId = PlaybackId.fromCreateVideoRequest(request)

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.KALTURA_REFERENCE)
        assertThat(playbackId.value).isEqualTo("ref-123")
    }

    @Test // A KALTURA type playbackId with kalturaEntryId value, when provider is KALTURA, and kalturaEntryId is provided
    fun `it should return a KALTURA PlaybackId when KALTURA and entryId is provided`() {
        val request = TestFactories.createCreateVideoRequest(
            playbackProvider = PlaybackProviderType.KALTURA.name,
            kalturaEntryId = "entry-123",
            kalturaReferenceId = "ref-123"
        );

        val playbackId = PlaybackId.fromCreateVideoRequest(request)

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(playbackId.value).isEqualTo("entry-123")
    }

    @Test
    fun `it should return a KALTURA_REFERENCE PlaybackId when KALTURA_REFERENCE and kalturaReferenceId is provided`() {
        val request = TestFactories.createCreateVideoRequest(
            playbackProvider = PlaybackProviderType.KALTURA_REFERENCE.name,
            kalturaReferenceId = "ref-123"
        );

        val playbackId = PlaybackId.fromCreateVideoRequest(request)

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.KALTURA_REFERENCE)
        assertThat(playbackId.value).isEqualTo("ref-123")
    }

    @Test
    fun `it should return a YOUTUBE PlaybackId when YOUTUBE and playbackId is provided`() {
        val request = TestFactories.createCreateVideoRequest(
            playbackProvider = PlaybackProviderType.YOUTUBE.name,
            playbackId = "youtube-123"
        );

        val playbackId = PlaybackId.fromCreateVideoRequest(request)

        assertThat(playbackId.type).isEqualTo(PlaybackProviderType.YOUTUBE)
        assertThat(playbackId.value).isEqualTo("youtube-123")
    }

//    @Test
//    fun `it should return a KALTURA PlaybackId from the request playbackId when KALTURA and entryId is not provided`() {}
}