package com.boclips.videos.service.application.subject

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.Administrator
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
                    subjects = video.subjects.items,
                    idToReplace = subjectId,
                    updatedSubject = updatedSubject
                )

                VideoUpdateCommand.ReplaceSubjects(videoId = video.videoId, subjects = newSubjects)
            }
        }

        collectionRepository.streamUpdate(CollectionFilter.HasSubjectId(subjectId), { collection ->
            val newSubjects = replaceSubject(
                subjects = collection.subjects,
                idToReplace = subjectId,
                updatedSubject = updatedSubject
            ).toSet()

            CollectionUpdateCommand.ReplaceSubjects(
                collectionId = collection.id,
                subjects = newSubjects,
                user = Administrator as User
            )
        })

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

