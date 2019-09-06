package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class UpdateSubject(
    private val subjectRepository: SubjectRepository,
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository,
    private val batchProcessingConfig: BatchProcessingConfig
) {
    operator fun invoke(subjectId: SubjectId, name: String?) {
        if (name == null) return

        subjectRepository.update(SubjectUpdateCommand.ReplaceName(subjectId = subjectId, name = name))
    }

    @BoclipsEventListener
    fun onUpdatedSubject(subjectChanged: SubjectChanged) {
        val subjectId = SubjectId(subjectChanged.subject.id.value)
        val updatedSubject = Subject(subjectId, subjectChanged.subject.name)

        videoRepository.streamUpdate { videos ->
            videos.map { video ->
                val newSubjects = replaceSubject(
                    subjects = video.subjects,
                    idToReplace = subjectId,
                    updatedSubject = updatedSubject
                )

                VideoUpdateCommand.ReplaceSubjects(videoId = video.videoId, subjects = newSubjects)
            }
        }

        collectionRepository.findAllBySubject(subjectId = subjectId).map { collection ->
            val newSubjects = replaceSubject(
                subjects = collection.subjects,
                idToReplace = subjectId,
                updatedSubject = updatedSubject
            ).toSet()

            val command = CollectionUpdateCommand.ReplaceSubjects(newSubjects)
            collectionRepository.update(collection.id, command)
        }
    }

    private fun replaceSubject(
        subjects: Collection<Subject>,
        idToReplace: SubjectId,
        updatedSubject: Subject
    ): List<Subject> {
        return subjects
            .filter { subject -> subject.id != idToReplace }
            .plus(updatedSubject)
    }
}

