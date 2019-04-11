package com.boclips.videos.service.application.video

import com.boclips.events.types.AnalysedVideo
import com.boclips.events.types.AnalysedVideoTopic
import com.boclips.events.config.Topics
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Topic
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.*
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import java.util.*

class UpdateAnalysedVideo(
        private val playbackRepository: PlaybackRepository,
        private val videoAssetRepository: VideoAssetRepository
) {
    companion object : KLogging()

    @StreamListener(Topics.ANALYSED_VIDEOS_SUBSCRIPTION)
    operator fun invoke(analysedVideo: AnalysedVideo) {
        try {
            doUpdateVideo(analysedVideo)
        } catch(e: Exception) {
            logger.error(e) { "Error updating video ${analysedVideo.videoId}" }
        }
    }

    private fun doUpdateVideo(analysedVideo: AnalysedVideo) {
        val videoId = analysedVideo.videoId
        logger.info { "Updating analysed video $videoId" }
        val video = try {
            videoAssetRepository.find(AssetId(videoId))
        } catch (e: Exception) {
            logger.error(e) { "Error looking up video $videoId" }
            return
        }

        if(video == null) {
            logger.info { "Could not find video $videoId" }
            return
        }

        logger.info { "Updating metadata of video $videoId" }
        try {
            updateMetadata(video, analysedVideo)
            logger.info { "Metadata of video $videoId updated"}
        } catch (e: Exception) {
            logger.error(e) { "Error updating metadata of video $videoId" }
        }

        if(analysedVideo.transcript.isBlank()) {
            logger.info { "Deleting any existing auto-generated captions of video $videoId" }
            playbackRepository.deleteAutoGeneratedCaptions(video.playbackId, analysedVideo.language)

            logger.info { "Not uploading captions of video $videoId because transcript is blank" }
            return
        }

        logger.info { "Uploading captions of video $videoId" }
        try {
            uploadCaptions(video, analysedVideo)
            logger.info { "Captions of video $videoId uploaded" }
        } catch(e: Exception) {
            logger.error(e) { "Error uploading captions of video $videoId" }
        }
    }

    private fun updateMetadata(video: VideoAsset, analysedVideo: AnalysedVideo) {
        videoAssetRepository.bulkUpdate(listOf(
                ReplaceLanguage(video.assetId, analysedVideo.language),
                ReplaceTranscript(video.assetId, analysedVideo.transcript),
                ReplaceTopics(video.assetId, convertTopics(analysedVideo.topics)),
                ReplaceKeywords(video.assetId, (video.keywords + analysedVideo.keywords.map { it.name }).toSet())
        ))
    }

    private fun uploadCaptions(video: VideoAsset, analysedVideo: AnalysedVideo) {
        playbackRepository.uploadCaptions(video.playbackId, analysedVideo.captions)
    }

    private fun convertTopics(topics: List<AnalysedVideoTopic>): Set<Topic> {
        return topics.map(Topic.Companion::fromAnalysedVideoTopic).toSet()
    }
}
