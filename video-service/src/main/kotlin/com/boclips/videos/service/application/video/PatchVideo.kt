package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.domain.service.VideoUpdateCommand
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoResourceToVideoUpdateConverter

class PatchVideo(
        private val videoService: VideoService
) {
    fun execute(id: String?, patch: VideoResource) {
        val updateCommand = VideoResourceToVideoUpdateConverter.convert(patch)

        val assetId = try {
            AssetId(value = id!!)
        } catch (ex: Exception) {
            throw VideoAssetNotFoundException()
        }

        videoService.update(assetId, VideoUpdateCommand.combine(updateCommand))
    }
}