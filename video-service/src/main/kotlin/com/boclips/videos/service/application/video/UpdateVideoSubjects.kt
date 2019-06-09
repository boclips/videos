package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.video.VideoSubjectClassified
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import org.springframework.cloud.stream.annotation.StreamListener

class UpdateVideoSubjects(
        private val videoRepository: VideoRepository,
        private val subjectRepository: SubjectRepository
) {

    @StreamListener(Subscriptions.VIDEO_SUBJECT_CLASSIFIED)
    operator fun invoke(videoSubjectClassified: VideoSubjectClassified) {
        val videoId = VideoId(videoSubjectClassified.videoId)
        val subjects = subjectRepository.findByIds(videoSubjectClassified.subjects.map { it.id })
        val updateCommand = VideoUpdateCommand.ReplaceSubjects(videoId, subjects)
        videoRepository.update(updateCommand)
    }
}
