package com.boclips.videos.service.application.subject

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class SubjectClassificationService(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    fun classifyVideo(video: Video) {
        if (!video.isPlayable()) {
            logger.info("Ignoring subject classification request of video ${video.videoId.value} because it is unplayable")
            return
        }

        if (video.type == LegacyVideoType.STOCK || video.type == LegacyVideoType.NEWS) {
            logger.info { "Ignoring subject classification request of video ${video.videoId.value} because it has type ${video.type}" }
            return
        }

        val videoSubjectClassificationRequested = VideoSubjectClassificationRequested.builder()
            .videoId(video.videoId.value)
            .title(video.title)
            .description(video.description)
            .build()

        eventBus.publish(videoSubjectClassificationRequested)
        logger.info { "Publishing subject classification requested event for video ${video.videoId.value}" }
    }

    @Async
    fun classifyVideosByContentPartner(contentPartner: String?): CompletableFuture<Unit> {
        logger.info { "Requesting subject classification for all instructional videos: $contentPartner" }
        val future = CompletableFuture<Unit>()
        val filter = contentPartner?.let { VideoFilter.ContentPartnerIs(it) } ?: VideoFilter.LegacyTypeIs(
            LegacyVideoType.INSTRUCTIONAL_CLIPS
        )
        videoRepository.streamAll(filter) { videos ->
            videos
                .forEach { video ->
                    classifyVideo(video)
                }
        }
        logger.info { "Requested subject classification for all instructional videos: $contentPartner" }
        future.complete(null)
        return future
    }
}
