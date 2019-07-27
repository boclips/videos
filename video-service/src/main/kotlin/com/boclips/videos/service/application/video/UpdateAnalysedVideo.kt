package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.domain.video.VideoAnalysedTopic
import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceKeywords
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceLanguage
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTopics
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTranscript
import mu.KLogging

class UpdateAnalysedVideo(
    private val playbackRepository: PlaybackRepository,
    private val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    @BoclipsEventListener
    operator fun invoke(videoAnalysed: VideoAnalysed) {
        val videoId = videoAnalysed.videoId
        logger.info { "Updating analysed video $videoId" }
        val video = try {
            videoRepository.find(VideoId(videoId))
        } catch (e: Exception) {
            logger.error(e) { "Error looking up video $videoId" }
            return
        }

        if (video == null) {
            logger.info { "Could not find video $videoId" }
            return
        }

        logger.info { "Updating metadata of video $videoId" }
        try {
            updateMetadata(video, videoAnalysed)

            logger.info { "Metadata of video $videoId updated" }
        } catch (e: Exception) {
            logger.error(e) { "Error updating metadata of video $videoId" }
        }

        if (videoAnalysed.transcript.isBlank()) {
            logger.info { "Deleting any existing auto-generated captions of video $videoId" }
            try {
                playbackRepository.deleteAutoGeneratedCaptions(video.playback.id, videoAnalysed.language)
                logger.info { "Not uploading captions of video $videoId because transcript is blank" }
            } catch (e: Exception) {
                logger.error(e) { "Error trying to delete existing captions of video $videoId" }
            }

            return
        }

        logger.info { "Uploading captions of video $videoId" }
        try {
            uploadCaptions(video, videoAnalysed)
            logger.info { "Captions of video $videoId uploaded" }
        } catch (e: Exception) {
            logger.error(e) { "Error uploading captions of video $videoId" }
        }
    }

    private fun updateMetadata(video: Video, analysedVideo: VideoAnalysed) {
        videoRepository.bulkUpdate(
            listOf(
                ReplaceLanguage(video.videoId, analysedVideo.language),
                ReplaceTranscript(video.videoId, analysedVideo.transcript),
                ReplaceTopics(video.videoId, convertTopics(analysedVideo.topics)),
                ReplaceKeywords(video.videoId, (video.keywords + analysedVideo.keywords.map { it.name }).toSet())
            )
        )

        val updatedVideo = videoRepository.find(video.videoId)!!
        videoSearchService.upsert(sequenceOf(updatedVideo))
    }

    private fun uploadCaptions(video: Video, analysedVideo: VideoAnalysed) {
        playbackRepository.uploadCaptions(video.playback.id, analysedVideo.captions)
    }

    private fun convertTopics(eventBus: List<VideoAnalysedTopic>): Set<Topic> {
        return eventBus.map(Topic.Companion::fromAnalysedVideoTopic).toSet()
    }
}
