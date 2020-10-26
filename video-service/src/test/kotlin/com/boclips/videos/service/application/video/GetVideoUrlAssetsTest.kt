package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URI

class GetVideoUrlAssetsTest {
    val captionService = mock<CaptionService>()
    val searchVideo = mock<SearchVideo>()
    val playbackProvider = mock<PlaybackProvider>()
    val getVideoUrlAsset = GetVideoUrlAssets(captionService, searchVideo, playbackProvider)

    @Test
    fun `returns captions & video content`() {
        val videoId = TestFactories.createVideoId().value
        whenever(searchVideo.byId(any(), user = any(), projection = any())).thenReturn(
            TestFactories.createVideo(
                videoId = videoId, playback = TestFactories.createKalturaPlayback(entryId = "entry-id")
            )
        )

        whenever(playbackProvider.getDownloadAssetUrl(eq(PlaybackId.from("entry-id", "KALTURA"))))
            .thenReturn(URI("entry-id"))

        val assetURLs = getVideoUrlAsset(videoId = videoId, user = UserFactory.sample())

        Assertions.assertThat(assetURLs.downloadVideoUrl).isEqualTo("something")
    }
}
