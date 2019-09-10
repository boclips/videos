package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging

class UpdateSubject(
    private val subjectRepository: SubjectRepository,
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository
) {
    companion object : KLogging()

    operator fun invoke(subjectId: SubjectId, name: String?) {
        if (name == null) return

        logger.info { "Triggered subject name update for $subjectId" }
        subjectRepository.update(SubjectUpdateCommand.ReplaceName(subjectId = subjectId, name = name))
    }

    @BoclipsEventListener
    fun onUpdatedSubject(subjectChanged: SubjectChanged) {
        val subjectId = SubjectId(subjectChanged.subject.id.value)
        val updatedSubject = Subject(subjectId, subjectChanged.subject.name)

        videoRepository.streamUpdate(VideoFilter.HasSubjectId(subjectId)) { videos ->
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

        logger.info { "Updated subject ${updatedSubject.id}" }
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

