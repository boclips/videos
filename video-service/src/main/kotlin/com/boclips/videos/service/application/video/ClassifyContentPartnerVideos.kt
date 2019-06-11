package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class ClassifyContentPartnerVideos(
    private val videoRepository: VideoRepository,
    private val classifyVideo: ClassifyVideo
)
{
    companion object : KLogging()

    @Async
    open operator fun invoke(contentPartner: String?): CompletableFuture<Unit> {
        logger.info { "Requesting subject classification for all instructional videos: $contentPartner" }
        val future = CompletableFuture<Unit>()
        val filter = contentPartner?.let { VideoFilter.ContentPartnerIs(it) } ?: VideoFilter.LegacyTypeIs(LegacyVideoType.INSTRUCTIONAL_CLIPS)
        videoRepository.streamAll(filter) { videos ->
            videos
                .forEach { video ->
                    classifyVideo(video.videoId.value)
                }
        }
        logger.info { "Requested subject classification for all instructional videos: $contentPartner" }
        future.complete(null)
        return future
    }
}
