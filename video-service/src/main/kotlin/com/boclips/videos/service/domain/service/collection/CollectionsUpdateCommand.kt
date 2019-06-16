package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.video.VideoId

sealed class CollectionsUpdateCommand {
    data class RemoveVideoFromAllCollections(val videoId: VideoId) : CollectionsUpdateCommand()
}