package com.boclips.videos.service.infrastructure.video.subject

import org.springframework.stereotype.Service

@Service
class SubjectRepository(val crudRepository: SubjectCrudRepository) {
    fun findByVideoIds(videoIds: List<Long>): List<VideoSubjectEntity> =
            crudRepository.findByVideoIdIn(videoIds)

    fun add(subjects: List<VideoSubjectEntity>) {
        subjects.forEach {
            validateSubjectName(it.subjectName)
            validateVideoId(it.videoId)
        }

        crudRepository.saveAll(subjects)
    }

    fun setSubjectsForVideo(videoId: Long, newSubjectNames: List<String>) {
        val newSubjects = newSubjectNames.map {
            validateSubjectName(it)
            VideoSubjectEntity(videoId, it)
        }

        val existingSubjects = crudRepository.findByVideoId(videoId)
        val subjectsToAdd = newSubjects - existingSubjects
        val subjectsToDelete = existingSubjects - newSubjects

        crudRepository.saveAll(subjectsToAdd)
        crudRepository.deleteAll(subjectsToDelete)
    }

    private fun validateSubjectName(subjectName: String?) {
        if (subjectName == null || subjectName.isEmpty()) {
            throw IllegalStateException()
        }
    }

    private fun validateVideoId(videoId: Long?) {
        if (videoId == null) {
            throw IllegalStateException()
        }
    }
}