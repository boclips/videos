package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories.createKalturaCaptionAsset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

internal class GenerateTranscriptsTest : AbstractSpringIntegrationTest() {

    @Test
    fun `generates transcripts for all marked videos`() {

        fakeKalturaClient.createMediaEntry("video-1", "ref-id-1", Duration.ofMinutes(10), MediaEntryStatus.READY)
        val humanCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", humanCaptions, "human content")
        saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-1"))


        fakeKalturaClient.createMediaEntry("video-2", "ref-id-2", Duration.ofMinutes(10), MediaEntryStatus.READY)
        fakeKalturaClient.createMediaEntry("video-3", "ref-id-3", Duration.ofMinutes(10), MediaEntryStatus.READY)

        saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-2"))
        saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-3"))

        createVideo()
        createVideo()
        fakeKalturaClient.addMediaEntry(MediaEntry.MediaEntryBuilder().)

        /*
        create three videos with kaltura media entries:
        1. with human generated captions and no transcripts
        2. with no captions
        3. with machine captions and transcripts

        run sync transcripts job

        assert that video
        1. generates transcripts and becomes unmarked
        2 and 3 are marked
         */
    }
}
