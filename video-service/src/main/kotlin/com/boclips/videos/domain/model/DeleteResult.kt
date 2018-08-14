package com.boclips.videos.domain.model

data class DeleteResult(val success: Boolean = true, val videosRemoved: Int = 0, val playlistEntriesRemoved: Long = 0, val orderlinesEntriesRemoved: Long = 0) {

    fun merge(other: DeleteResult) = DeleteResult(
            success = success && other.success,
            videosRemoved = videosRemoved + other.videosRemoved,
            playlistEntriesRemoved = playlistEntriesRemoved + other.playlistEntriesRemoved,
            orderlinesEntriesRemoved = orderlinesEntriesRemoved + other.orderlinesEntriesRemoved
    )
}