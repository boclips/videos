package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.service.VideoService
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildSearchIndex(private val videoService: VideoService) {

    @Async
    open fun execute(): CompletableFuture<Unit> {
        videoService.rebuildSearchIndex()
        return CompletableFuture.completedFuture(null)
    }
}