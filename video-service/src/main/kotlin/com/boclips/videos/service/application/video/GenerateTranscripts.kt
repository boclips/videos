package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
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
        logger.info("Starting a generating transcripts for marked videos")

        videoRepository.streamAll(VideoFilter.IsMarkedForTranscriptGeneration) { videos -> // maybe use streamUpdate
            videos.forEach { video ->
                val humanGeneratedCaption =
                    captionService.getCaption(videoId = video.videoId, humanGeneratedOnly = true)

                humanGeneratedCaption?.let { caption -> // put this thing to the captionService?

                    val transcript = captionConverter.convertToTranscript(caption)

                    videoRepository.bulkUpdate(
                        listOf(
                            VideoUpdateCommand.ReplaceTranscript(
                                videoId = video.videoId,
                                transcript = transcript, // wrong, we should parse from caption to transcript string
                                isHumanGenerated = true
                            ),
                            VideoUpdateCommand.ReplaceTranscriptRequested(
                                videoId = video.videoId,
                                isTranscriptRequested = false
                            )
                        )
                    )
                }
            }
        }
    }
}
