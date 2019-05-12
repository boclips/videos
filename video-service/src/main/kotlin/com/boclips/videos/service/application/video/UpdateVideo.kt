package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.video.VideoResource

class UpdateVideo(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(id: String?, patch: VideoResource) {
        val videoId = try {
            resolveToAssetId(id)
        } catch (ex: Exception) {
            throw VideoNotFoundException()
        }

        val updateCommands = VideoUpdatesConverter.convert(videoId, patch)

        videoRepository.bulkUpdate(updateCommands)
    }

    private fun resolveToAssetId(idOrAlias: String?): VideoId {
        if (idOrAlias == null) throw VideoNotFoundException()

        return if (SearchVideo.isAlias(idOrAlias)) {
            videoRepository.resolveAlias(idOrAlias) ?: throw VideoNotFoundException()
        } else {
            videoRepository.find(VideoId(value = idOrAlias))?.videoId
                ?: throw VideoNotFoundException()
        }
    }
}