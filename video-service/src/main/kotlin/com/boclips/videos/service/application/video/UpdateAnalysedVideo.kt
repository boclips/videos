package com.boclips.videos.service.application.video

import com.boclips.events.types.AnalysedVideo
import com.boclips.events.types.Topics
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import org.springframework.cloud.stream.annotation.StreamListener

class UpdateAnalysedVideo(
        private val playbackRepository: PlaybackRepository,
        private val videoAssetRepository: VideoAssetRepository
) {
    @StreamListener(Topics.ANALYSED_VIDEOS_SUBSCRIPTION)
    operator fun invoke(analysedVideo: AnalysedVideo) {
        videoAssetRepository.find(AssetId(analysedVideo.videoId))?.let { video ->
            playbackRepository.uploadCaptions(video.playbackId, analysedVideo.captions)
        }
    }
}
