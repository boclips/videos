package com.boclips.videos.service.domain.service.video

import com.boclips.kalturaclient.KalturaCaptionManager
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat.SRT
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.io.FileInputStream
import com.boclips.kalturaclient.captionasset.CaptionFormat as KalturaCaptionFormat

class CaptionServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var captionService: CaptionService

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var kalturaClient: KalturaClient

    @Test
    fun `retrieves the caption content of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "captions content to retrieve")

        assertThat(captionService.getCaptionContent(videoId)).isEqualTo("captions content to retrieve")
    }

    @Test
    fun `retrieves full caption details of a video`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)",
            captionFormat = KalturaCaptionFormat.SRT
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "captions content to retrieve")

        assertThat(captionService.getAvailableCaptions(videoId)).containsExactly(
            Caption(
                content = "captions content to retrieve",
                format = SRT,
                default = false
            )
        )
    }

    @Test
    fun `throws when attempting to fetch captions for a non existing video`() {
        assertThrows<VideoNotFoundException> { captionService.getAvailableCaptions(TestFactories.createVideoId()) }
    }

    @Test
    fun `throws when attempting to fetch captions for a non boclips hosted video`() {
        val videoId = saveVideo(playbackId = TestFactories.createYoutubePlayback().id)
        assertThrows<UnsupportedCaptionsException> { captionService.getAvailableCaptions(videoId) }
    }

    @Test
    fun `Updates the caption content of a video`() {
        val video = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "previous captions content")

        captionService.updateCaptionContent(
            video, """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent()
        )

        val captionFiles = fakeKalturaClient.getCaptionsForVideo("playback-id")

        assertThat(captionFiles).hasSize(1)
        assertThat(fakeKalturaClient.getCaptionContent(captionFiles.first().id)).isEqualTo(
            """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent()
        )
    }

    @Test
    fun `Updating a video's captions also updates its transcripts`() {
        val video = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "previous captions content")

        captionService.updateCaptionContent(
            video, """WEBVTT FILE

                        1
                        00:01.981 --> 00:04.682
                        We're quite content to be the odd<br>browser out.

                        2
                        00:05.302 --> 00:08.958
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.

                        3
                        00:09.526 --> 00:11.324
                        We don't have a profit margin.""".trimIndent()
        )

        val updatedVideo = videoRepository.find(video)

        assertThat(updatedVideo?.voice?.transcript).isEqualTo(
            """
                        We're quite content to be the odd<br>browser out.
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.
                        We don't have a profit margin.""".trimIndent()
        )
    }

    @Test
    fun `requests video captions`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))

        captionService.requestCaption(videoId)

        assertThat(kalturaClient.getCaptionStatus("playback-id")).isEqualTo(KalturaCaptionManager.CaptionStatus.REQUESTED)
    }

    @Test
    fun `throws when attempting to request captions for a non existing video`() {
        assertThrows<VideoNotFoundException> { captionService.requestCaption(TestFactories.createVideoId()) }
    }

    @Test
    fun `throws when attempting to request captions for a non boclips hosted video`() {
        val videoId = saveVideo(playbackId = TestFactories.createYoutubePlayback().id)
        assertThrows<UnsupportedCaptionsException> { captionService.requestCaption(videoId) }
    }

    @Test
    fun `translates english srt and text to spanish srt`() {
        val sourceSRT = """
            1
            00:00:09,800 --> 00:00:15,000
             10 apple pie it's kind of ironic that The Benchmark
            
            2
            00:00:15,000 --> 00:00:16,200
             for being American.
            
            3
            00:00:16,200 --> 00:00:21,000
             Standard but it's truth apple pie isn't as American
            
            4
            00:00:21,000 --> 00:00:24,100
             as apple pie historically speaking the first known
            
            5
            00:00:24,100 --> 00:00:27,300
             reference to the pastry treat then cold tatra sandopolis
            
            6
            00:00:27,300 --> 00:00:29,700
             comes from 14th century England,
            
            7
            00:00:29,700 --> 00:00:32,700
             but both pie starkly different from the ones we know/
            
            8
            00:00:32,700 --> 00:00:33,000
             today.
            
            9
            00:00:33,000 --> 00:00:36,700
             They didn't have crust because sugar was around $23
            
            10
            00:00:36,700 --> 00:00:38,400
             per kilogram and adjusted figures.
        """.trimIndent()

        val sourceTXT = """
            10 apple pie it's kind of ironic that The Benchmark
            for being American.
            Standard but it's truth apple pie isn't as American
            as apple pie historically speaking the first known
            reference to the pastry treat then cold tatra sandopolis
            comes from 14th century England,
            but both pie starkly different from the ones we know
            today.
            They didn't have crust because sugar was around $23
            per kilogram and adjusted figures.
        """.trimIndent()

        val outputSRT = captionService.translateToSpanish(text = sourceTXT, sourceSrt = sourceSRT)
        val expectedSRT = """
            1
            00:00:09,800 --> 00:00:15,000
            10 tarta de manzana es un poco irónico que The Benchmark
            
            2
            00:00:15,000 --> 00:00:16,200
            por ser estadounidense.
            
            3
            00:00:16,200 --> 00:00:21,000
            Estándar, pero es verdad, la tarta de manzana no es tan estadounidense
            
            4
            00:00:21,000 --> 00:00:24,100
            como tarta de manzana, históricamente hablando, la primera conocida
            
            5
            00:00:24,100 --> 00:00:27,300
            referencia a la golosina de repostería luego fría tatra sandopolis
            
            6
            00:00:27,300 --> 00:00:29,700
            proviene de la Inglaterra del siglo XIV,
            
            7
            00:00:29,700 --> 00:00:32,700
            pero ambos son completamente diferentes a los que conocemos
            
            8
            00:00:32,700 --> 00:00:33,000
            hoy dia.
            
            9
            00:00:33,000 --> 00:00:36,700
            No tenían corteza porque el azúcar costaba alrededor de $ 23
            
            10
            00:00:36,700 --> 00:00:38,400
            por kilogramo y cifras ajustadas.
        """.trimIndent()

        assertThat(outputSRT).isEqualTo(expectedSRT)
    }

    @Test
    fun `it translates files`() {
        print("reading files")
        File("/Users/pancake/workspace/videos/video-service/mp3s/").walk().filter { it.name.endsWith(".txt") }.forEach {
            println(it.absolutePath)
            val inputTxt = FileInputStream(it.absolutePath).readBytes().decodeToString()
            val inputSrt = FileInputStream(it.absolutePath.replace(".txt", ".srt")).readBytes().decodeToString()
            val spanishSRT = captionService.translateToSpanish(text = inputTxt, sourceSrt = inputSrt)
            File(it.absolutePath.replace(".txt", "-SPA.srt")).writeText(spanishSRT)
        }


    }
}
