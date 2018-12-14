package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset

interface VideoUpdateCommand {
    fun update(video: Video): Video

    companion object {
        private class VideoUpdateChain(private val updateCommands: List<VideoUpdateCommand>) : VideoUpdateCommand {
            override fun update(video: Video): Video {
                return updateCommands.fold(video) { v, u -> u.update(v) }
            }
        }

        fun combine(updateCommands: List<VideoUpdateCommand>): VideoUpdateCommand {
            return VideoUpdateChain(updateCommands)
        }
    }
}

abstract class VideoAssetUpdate : VideoUpdateCommand {
    override fun update(video: Video): Video {
        return Video(
                asset = updateAsset(video.asset),
                playback = video.playback
        )
    }

    protected abstract fun updateAsset(video: VideoAsset): VideoAsset
}

data class VideoSubjectsUpdate(val subjects: Set<Subject>) : VideoAssetUpdate() {
    override fun updateAsset(video: VideoAsset) = video.copy(subjects = subjects)
}

data class VideoTitleUpdate(val title: String) : VideoAssetUpdate() {
    override fun updateAsset(video: VideoAsset) = video.copy(title = title)
}
