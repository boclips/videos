package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.CaptionConverter
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class GenerateTranscripts(
    private val videoRepository: VideoRepository,
    private val captionService: CaptionService,
    private val captionConverter: CaptionConverter
) {
    companion object : KLogging()

    operator fun invoke() {
        logger.info("Starting generating transcripts for marked videos")

        videoRepository.streamUpdate(VideoFilter.IsMarkedForTranscriptGeneration) { videos ->
            videos.flatMap { video ->
                captionService.getCaption(videoId = video.videoId, humanGeneratedOnly = true)?.let { caption ->
                    captionConverter.convertToTranscript(caption)?.let { transcript ->
                        getMarkedVideoUpdateCommands(video.videoId, transcript)
                    }
                } ?: emptyList()
            }
        }
    }

    private fun getMarkedVideoUpdateCommands(videoId: VideoId, transcript: String): List<VideoUpdateCommand> =
        listOf(
            VideoUpdateCommand.ReplaceTranscript(
                videoId = videoId,
                transcript = transcript,
                isHumanGenerated = true
            ),
            VideoUpdateCommand.ReplaceTranscriptRequested(
                videoId = videoId,
                isTranscriptRequested = false
            )
        )
}
