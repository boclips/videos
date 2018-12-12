package com.boclips.videos.service.infrastructure.video.subject

import org.springframework.stereotype.Service

@Service
class SubjectRepository(val crudRepository: SubjectCrudRepository) {
    fun findByVideoIds(videoIds: List<Long>): List<VideoSubjectEntity> = crudRepository.findByVideoIdIn(videoIds)

    fun create(subjects: List<VideoSubjectEntity>) {
        crudRepository.saveAll(subjects)
    }

    fun setSubjectsForVideo(videoId: Long, newSubjectNames: List<String>) {
        val newSubjects = newSubjectNames.map { VideoSubjectEntity(videoId, it) }
        val existingSubjects = crudRepository.findByVideoId(videoId)
        val subjectsToAdd = newSubjects - existingSubjects
        val subjectsToDelete = existingSubjects - newSubjects

        crudRepository.saveAll(subjectsToAdd)
        crudRepository.deleteAll(subjectsToDelete)
    }
}