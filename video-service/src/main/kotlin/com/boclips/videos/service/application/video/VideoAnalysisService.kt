package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.domain.video.VideoAnalysedTopic
import com.boclips.eventbus.events.video.RetryVideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import java.util.Locale

open class VideoAnalysisService(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus,
    private val playbackRepository: PlaybackRepository
) {
    companion object : KLogging()

    open fun analyseVideosOfChannel(channelId: String, language: Locale?) {
        videoRepository.streamAll(VideoFilter.IsVoicedWithoutTranscript(ChannelId(channelId))) { allVideos ->
            allVideos.windowed(size = 1000, step = 1000, partialWindows = true)
                .forEachIndexed { batchIndex, batchOfVideos ->
                    logger.info { "Dispatching analyse playable video events of channel:$channelId batch: $batchIndex" }
                    batchOfVideos.forEach { video ->
                        analysePlayableVideo(video.videoId.value, language)
                    }
                }
        }
    }

    fun analysePlayableVideo(id: String, language: Locale?, retry: Boolean? = false) {
        val videoId = VideoId(id)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)
        if (!video.isPlayable()) throw VideoPlaybackNotFound()

        val playback = video.playback as? VideoPlayback.StreamPlayback ?: throw VideoNotAnalysableException()

        if (!video.types.contains(VideoType.INSTRUCTIONAL_CLIPS) && !video.types.contains(VideoType.NEWS)) {
            logger.info { "Analysis of video $id NOT requested because its legacy type is ${video.types}" }
            return
        }

        when (retry) {
            true -> {
                eventBus.publish(
                    RetryVideoAnalysisRequested.builder()
                        .videoId(video.videoId.value)
                        .videoUrl(playback.downloadUrl)
                        .language(language)
                        .build()
                )
                logger.info { "Retrying Analysis of video $id requested" }
            }
            else -> {
                eventBus.publish(
                    VideoAnalysisRequested.builder()
                        .videoId(video.videoId.value)
                        .videoUrl(playback.downloadUrl)
                        .language(language)
                        .build()
                )
                logger.info { "Analysis of video $id requested" }
            }
        }
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
            listOfNotNull(
                VideoUpdateCommand.ReplaceLanguage(video.videoId, video.voice.language ?: analysedVideo.language),
                getTranscriptUpdateCommand(video, analysedVideo),
                VideoUpdateCommand.ReplaceTopics(video.videoId, convertTopics(analysedVideo.topics)),
                VideoUpdateCommand.ReplaceKeywords(
                    video.videoId,
                    (video.keywords + analysedVideo.keywords.map { it.name }).toSet()
                )
            )
        )
    }

    private fun getTranscriptUpdateCommand(video: Video, analysedVideo: VideoAnalysed): VideoUpdateCommand? {
        val hasHumanGeneratedTranscript: Boolean = video.voice.transcript?.isHumanGenerated ?: false
        return if (!hasHumanGeneratedTranscript) {
            VideoUpdateCommand.ReplaceTranscript(
                videoId = video.videoId,
                transcript = analysedVideo.transcript,
                isHumanGenerated = false
            )
        } else {
            null
        }
    }

    private fun uploadCaptions(video: Video, analysedVideo: VideoAnalysed) {
        playbackRepository.uploadCaptions(video.playback.id, analysedVideo.captions)
    }

    private fun convertTopics(eventBus: List<VideoAnalysedTopic>): Set<Topic> {
        return eventBus.map(Topic.Companion::fromAnalysedVideoTopic).toSet()
    }
}
