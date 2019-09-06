package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class UpdateSubject(
    private val subjectRepository: SubjectRepository,
    private val videoRepository: VideoRepository,
    private val batchProcessingConfig: BatchProcessingConfig
) {
    operator fun invoke(subjectId: SubjectId, name: String?) {
        if (name == null) return

        subjectRepository.update(SubjectUpdateCommand.ReplaceName(subjectId = subjectId, name = name))
    }

    @BoclipsEventListener
    fun onUpdatedSubject(updatedSubject: SubjectChanged) {
        val subjectId = SubjectId(updatedSubject.subject.id.value)

        videoRepository.streamAll(VideoFilter.HasSubjectId(subjectId)) { videos ->
            videos.windowed(
                size = batchProcessingConfig.videoBatchSize,
                step = batchProcessingConfig.videoBatchSize,
                partialWindows = true
            ).forEach { windowedVideos ->
                val commands = windowedVideos.map { video ->
                    val newSubjects = video.subjects
                        .filter { subject -> subject.id != subjectId }
                        .plus(Subject(subjectId, updatedSubject.subject.name))

                    VideoUpdateCommand.ReplaceSubjects(videoId = video.videoId, subjects = newSubjects)
                }

                videoRepository.bulkUpdate(commands)
            }
        }
    }
}

