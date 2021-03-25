package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.application.video.UpdateCaptions
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.VideoId
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.translate.v3.*
import java.io.FileInputStream

class CaptionService(
    private val videoRepository: VideoRepository,
    private val playbackRepository: PlaybackRepository,
    private val captionValidator: CaptionValidator
) {

    fun getAvailableCaptions(videoId: VideoId): List<Caption> {
        return videoRepository.find(videoId)
            ?.let { video ->
                if (video.isBoclipsHosted()) {
                    playbackRepository.getCaptions(playbackId = video.playback.id)
                } else {
                    throw UnsupportedCaptionsException(video)
                }
            } ?: throw VideoNotFoundException(videoId)
    }

    fun getCaptionContent(videoId: VideoId): String? {
        return videoRepository.find(videoId)?.let { video ->
            playbackRepository.getCaptions(playbackId = video.playback.id).firstOrNull()?.content
        }
    }

    fun requestCaption(videoId: VideoId) {
        videoRepository.find(videoId)?.let { video ->
            (video.playback as? VideoPlayback.StreamPlayback)?.let {
                playbackRepository.requestCaptions(playbackId = video.playback.id)
            } ?: throw UnsupportedCaptionsException(video)
        } ?: throw VideoNotFoundException(videoId)
    }

    fun updateCaptionContent(videoId: VideoId, captionContent: String) {
        if (captionValidator.checkValid(captionContent)) {
            videoRepository.find(videoId)?.let { video ->
                playbackRepository.updateCaptionContent(video.playback.id, captionContent)

                videoRepository.update(
                    VideoUpdateCommand.ReplaceTranscript(
                        video.videoId, captionValidator.parse(captionContent).joinToString(separator = "\n")
                    )
                )

                UpdateCaptions.logger.info { "Updated captions for ${video.videoId}" }
            }
        }
    }

    fun translateToSpanish(text: String, sourceSrt: String): String {
        val projectId = "boclips-prod"
        val targetLanguage = "es"
//        val credentials = ServiceAccountCredentials.fromStream(FileInputStream("boclips-prod-6bba4368a748.json"))
//        val settings =
//            TranslationServiceSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//                .build()

        val client: TranslationServiceClient = TranslationServiceClient.create()
        val parent: LocationName = LocationName.of(projectId, "global")

        // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
        val request: TranslateTextRequest =
            TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode(targetLanguage)
                .addContents(text)
                .build()

        val response: TranslateTextResponse = client.translateText(request);

        val translatedText = response.getTranslations(0).translatedText.lines()
        val timestampPattern = Regex("[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3} --> [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}")
        val lineIndexPattern = Regex("^[-,0-9]+\$")
        var lineToReplace = 0

        val translatedSRT = sourceSrt.lines().map { line ->
            if (
                line.isEmpty() ||
                line.matches(timestampPattern) ||
                line.matches(lineIndexPattern)
            ) {
                return@map line
            } else {
                val newLine = translatedText[lineToReplace]
                lineToReplace++;
                return@map newLine
            }
        }.joinToString(separator = "\n")

        return translatedSRT
    }
}
