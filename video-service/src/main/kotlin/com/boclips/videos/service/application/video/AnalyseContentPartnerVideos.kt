package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetFilter
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import java.util.*

class AnalyseContentPartnerVideos(
        private val videoAssetRepository: VideoAssetRepository,
        private val analyseVideo: AnalyseVideo
) {
    operator fun invoke(contentPartner: String, language: Locale?) {
        videoAssetRepository.streamAll(VideoAssetFilter.ContentPartnerIs(contentPartner)) { videos ->
            videos.forEach { video ->
                analyseVideo(video.assetId.value, language)
            }
        }
    }
}
