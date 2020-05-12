package com.boclips.videos.service.domain.service.subject

import com.boclips.videos.service.application.subject.UpdateSubject
import com.boclips.videos.service.domain.model.collection.CollectionFilter
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.presentation.Administrator

class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val videoRepository: VideoRepository,
    private val collectionRepository: CollectionRepository
) {
    fun renameOnly(subjectId: SubjectId, name: String) {
        subjectRepository.update(SubjectUpdateCommand.ReplaceName(subjectId = subjectId, name = name))
    }

    fun removeReferences(subjectId: SubjectId, user: User) {
        subjectRepository.delete(subjectId)

        collectionRepository.streamUpdate(CollectionFilter.HasSubjectId(subjectId), { collection ->
            CollectionUpdateCommand.RemoveSubjectFromCollection(
                collectionId = collection.id,
                subjectId = subjectId,
                user = user
            )
        }, {})

        videoRepository.streamUpdate(VideoFilter.HasSubjectId(subjectId)) { videos: List<Video> ->
            videos.map { video ->
                VideoUpdateCommand.RemoveSubject(videoId = video.videoId, subjectId = subjectId)
            }
        }
    }

    fun replaceReferences(subject: Subject) {
        videoRepository.streamUpdate(VideoFilter.HasSubjectId(subject.id)) { videos ->
            videos.map { video ->
                val newSubjects = replaceOneSubject(
                    subjects = video.subjects.items,
                    idToReplace = subject.id,
                    updatedSubject = subject
                )

                VideoUpdateCommand.ReplaceSubjects(videoId = video.videoId, subjects = newSubjects)
            }
        }

        collectionRepository.streamUpdate(CollectionFilter.HasSubjectId(subject.id), { collection ->
            val newSubjects = replaceOneSubject(
                subjects = collection.subjects,
                idToReplace = subject.id,
                updatedSubject = subject
            ).toSet()

            CollectionUpdateCommand.ReplaceSubjects(
                collectionId = collection.id,
                subjects = newSubjects,
                user = Administrator as User
            )
        })

        UpdateSubject.logger.info { "Updated subject ${subject.id}" }
    }

    private fun replaceOneSubject(
        subjects: Collection<Subject>,
        idToReplace: SubjectId,
        updatedSubject: Subject
    ): List<Subject> {
        return subjects
            .filter { subject -> subject.id != idToReplace }
            .plus(updatedSubject)
    }
}
