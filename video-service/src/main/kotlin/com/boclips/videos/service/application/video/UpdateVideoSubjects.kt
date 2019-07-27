package com.boclips.videos.service.application.video

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class UpdateVideoSubjects(
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    @BoclipsEventListener
    operator fun invoke(videoSubjectClassified: VideoSubjectClassified) {
        val videoId = VideoId(videoSubjectClassified.videoId)
        try {
            val subjects = subjectRepository.findByIds(videoSubjectClassified.subjects.map { it.value })
            if (subjects.isNotEmpty()) {
                val updateCommand = VideoUpdateCommand.ReplaceSubjects(videoId, subjects)
                val updatedVideo = videoRepository.update(updateCommand)
                videoSearchService.upsert(sequenceOf(updatedVideo))
                logger.info { "Updates subjects of video ${videoId.value}: ${subjects.joinToString(", ") { it.name }}" }
            } else {
                logger.info(
                    "Not found",
                    "Subject with id ${videoSubjectClassified.subjects.map { it.value }} cannot be found"
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Updating subjects of video ${videoId.value} failed and will not be retried" }
        }
    }
}
