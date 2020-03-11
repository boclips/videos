package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.video.VideoAnalysedTopic
import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import java.util.Locale

class VideoAnalysisService(
    private val videoRepository: VideoRepository,
    private val videoService: VideoService,
    private val eventBus: EventBus,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    fun analyseVideosOfContentPartner(contentPartner: String, language: Locale?) {
        videoRepository.streamAll(VideoFilter.ContentPartnerNameIs(contentPartner)) { videos ->
            videos.forEach { video ->
                analysePlayableVideo(video.videoId.value, language)
            }
        }
    }

    fun analysePlayableVideo(videoId: String, language: Locale?) {
        val video = videoService.getPlayableVideo(
            videoId = VideoId(value = videoId)
        )
        val playback = video.playback as? VideoPlayback.StreamPlayback ?: throw VideoNotAnalysableException()

        if (video.type != ContentType.INSTRUCTIONAL_CLIPS) {
            logger.info { "Analysis of video $videoId NOT requested because its legacy type is ${video.type.name}" }
            return
        }

        if (video.playback.duration.seconds <= 20) {
            logger.info { "Analysis of video $videoId NOT requested because it's too short" }
            return
        }

        val videoToAnalyse = VideoAnalysisRequested.builder()
            .videoId(video.videoId.value)
            .videoUrl(playback.downloadUrl)
            .language(language)
            .build()

        eventBus.publish(videoToAnalyse)

        logger.info { "Analysis of video $videoId requested" }
    }

    @BoclipsEventListener
    fun videoAnalysed(videoAnalysed: VideoAnalysed) {
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
                VideoUpdateCommand.ReplaceLanguage(video.videoId, video.language ?: analysedVideo.language),
                VideoUpdateCommand.ReplaceTranscript(video.videoId, analysedVideo.transcript),
                VideoUpdateCommand.ReplaceTopics(video.videoId, convertTopics(analysedVideo.topics)),
                VideoUpdateCommand.ReplaceKeywords(
                    video.videoId,
                    (video.keywords + analysedVideo.keywords.map { it.name }).toSet()
                )
            )
        )
    }

    private fun uploadCaptions(video: Video, analysedVideo: VideoAnalysed) {
        playbackRepository.uploadCaptions(video.playback.id, analysedVideo.captions)
    }

    private fun convertTopics(eventBus: List<VideoAnalysedTopic>): Set<Topic> {
        return eventBus.map(Topic.Companion::fromAnalysedVideoTopic).toSet()
    }
}
