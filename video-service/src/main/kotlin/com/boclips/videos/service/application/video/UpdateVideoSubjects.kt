package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.video.VideoSubjectClassified
import com.boclips.videos.service.domain.model.subjects.SubjectRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class UpdateVideoSubjects(
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEO_SUBJECT_CLASSIFIED)
    operator fun invoke(videoSubjectClassified: VideoSubjectClassified) {
        val videoId = VideoId(videoSubjectClassified.videoId)
        try {
            val subjects = subjectRepository.findByIds(videoSubjectClassified.subjects.map { it.id })
            if(subjects.isNotEmpty()) {
                val updateCommand = VideoUpdateCommand.ReplaceSubjects(videoId, subjects)
                val updatedVideo = videoRepository.update(updateCommand)
                videoSearchService.upsert(sequenceOf(updatedVideo))
                logger.info { "Updates subjects of video ${videoId.value}: ${subjects.joinToString(", ") { it.name }}" }
            } else {
                logger.info("Not found", "Subject with id ${videoSubjectClassified.subjects.map { it.id }} cannot be found")
            }

        } catch (e: Exception) {
            logger.error(e) { "Updating subjects of video ${videoId.value} failed and will not be retried" }
        }
    }
}
