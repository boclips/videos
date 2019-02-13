package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.PartialVideoAsset
import com.boclips.videos.service.domain.model.asset.Subject

class VideoResourceToPartialVideoAssetConverter {
    // Incomplete implementation
    companion object {
        fun convert(videoResource: VideoResource): PartialVideoAsset {
            return if (videoResource.subjects != null) {
                val subjects = videoResource.subjects.map { Subject(it) }.toSet()
                PartialVideoAsset(subjects = subjects)
            } else {
                PartialVideoAsset()
            }
        }
    }
}
