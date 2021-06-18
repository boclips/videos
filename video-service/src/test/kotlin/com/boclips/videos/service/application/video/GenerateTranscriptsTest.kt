package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

internal class GenerateTranscriptsTest : AbstractSpringIntegrationTest() {

    val srtCaptionContent = """
1
00:00:00,500 --> 00:00:02,000
The Web is always changing

2
00:00:02,500 --> 00:00:04,300
and the way we access it is changing

"""

    @Autowired
    lateinit var generateTranscripts: GenerateTranscripts

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `generates transcripts for all marked videos`() {
        fakeKalturaClient.createMediaEntry("video-1", "ref-id-1", Duration.ofMinutes(10), MediaEntryStatus.READY)
        fakeKalturaClient.createMediaEntry("video-2", "ref-id-2", Duration.ofMinutes(10), MediaEntryStatus.READY)
        fakeKalturaClient.createMediaEntry("video-3", "ref-id-3", Duration.ofMinutes(10), MediaEntryStatus.READY)

        val humanCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English",
            captionFormat = CaptionFormat.SRT
        )
        val machineCaptions =
            createKalturaCaptionAsset(language = KalturaLanguage.ENGLISH, label = "English (auto-generated)")

        fakeKalturaClient.createCaptionForVideo("video-1", humanCaptions, srtCaptionContent)
        fakeKalturaClient.createCaptionForVideo("video-2", machineCaptions, srtCaptionContent)

        val videoWithHumanGeneratedCaption =
            saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-1"))
        val videoWithAutoGeneratedCaption =
            saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-2"))
        val videoWithNoCaption =
            saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "video-3"))

        markVideoAsTranscriptRequested(videoWithHumanGeneratedCaption)
        markVideoAsTranscriptRequested(videoWithNoCaption)
        markVideoAsTranscriptRequested(videoWithAutoGeneratedCaption)
        videoRepository.find(videoWithNoCaption)
        generateTranscripts()

        val processedVideo = videoRepository.find(videoWithHumanGeneratedCaption)!!
        assertThat(processedVideo.voice.transcript).isEqualTo("""The Web is always changing
and the way we access it is changing""")
        assertThat(processedVideo.voice.isTranscriptHumanGenerated).isTrue
        assertThat(processedVideo.voice.isTranscriptRequested).isFalse

        val unprocessedVideo1 = videoRepository.find(videoWithNoCaption)!!
        assertThat(unprocessedVideo1.voice.isTranscriptRequested).isTrue
        assertThat(unprocessedVideo1.voice.transcript).isNullOrEmpty()

        val unprocessedVideo2 = videoRepository.find(videoWithAutoGeneratedCaption)!!
        assertThat(unprocessedVideo2.voice.isTranscriptRequested).isTrue
        assertThat(unprocessedVideo2.voice.transcript).isNullOrEmpty()
    }

    private fun markVideoAsTranscriptRequested(videoId: VideoId) {
        updateVideo(
            videoId.value,
            UpdateVideoRequest(transcriptRequested = true),
            user = UserFactory.sample(id = "admin@boclips.com")
        )
    }
}
