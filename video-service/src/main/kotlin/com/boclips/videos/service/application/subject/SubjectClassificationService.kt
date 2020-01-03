package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class SubjectClassificationService(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus,
    private val subjectRepository: SubjectRepository
) {
    companion object : KLogging()

    fun classifyVideo(video: Video) {
        if (!video.isPlayable()) {
            logger.info("Ignoring subject classification request of video ${video.videoId.value} because it is unplayable")
            return
        }

        if (video.type == ContentType.STOCK || video.type == ContentType.NEWS) {
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
        val filter = contentPartner?.let { VideoFilter.ContentPartnerNameIs(it) } ?: VideoFilter.ContentTypeIs(
            ContentType.INSTRUCTIONAL_CLIPS
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

    @BoclipsEventListener
    fun videoClassified(videoSubjectClassified: VideoSubjectClassified) {
        val videoId = VideoId(videoSubjectClassified.videoId)
        try {
            val subjects = subjectRepository.findByIds(videoSubjectClassified.subjects.map { it.value })
            if (subjects.isNotEmpty()) {
                val updateCommand = VideoUpdateCommand.ReplaceSubjects(videoId, subjects)
                videoRepository.update(updateCommand)
                logger.info { "Update subjects of video ${videoId.value}: ${subjects.joinToString(", ") { it.name }}" }
            } else {
                logger.info("Subjects with ids ${videoSubjectClassified.subjects.map { it.value }} cannot be found for video ${videoId.value}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Updating subjects of video ${videoId.value} failed and will not be retried" }
        }
    }
}
