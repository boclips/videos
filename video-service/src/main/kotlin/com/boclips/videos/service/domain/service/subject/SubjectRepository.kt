package com.boclips.videos.service.domain.service.subject

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun create(name: String): Subject
}
