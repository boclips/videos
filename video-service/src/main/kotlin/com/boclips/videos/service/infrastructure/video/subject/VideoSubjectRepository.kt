package com.boclips.videos.service.infrastructure.video.subject

import org.springframework.stereotype.Service

@Service
class VideoSubjectRepository(val videoSubjectCrudRepository: VideoSubjectCrudRepository) {
    fun findByVideoIdIn(videoIds: List<Long>): List<VideoSubjectEntity> = videoSubjectCrudRepository.findByVideoIdIn(videoIds)

    fun create(subjects: List<VideoSubjectEntity>) {
        videoSubjectCrudRepository.saveAll(subjects)
    }

    fun setSubjectsForVideo(videoId: Long, newSubjectNames: List<String>) {
        val newSubjects = newSubjectNames.map { VideoSubjectEntity(videoId, it) }
        val existingSubjects = videoSubjectCrudRepository.findByVideoId(videoId)
        val subjectsToAdd = newSubjects - existingSubjects
        val subjectsToDelete = existingSubjects - newSubjects

        videoSubjectCrudRepository.saveAll(subjectsToAdd)
        videoSubjectCrudRepository.deleteAll(subjectsToDelete)
    }
}