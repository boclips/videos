package com.boclips.videos.service.domain.model.subjects

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun findByIds(ids: Iterable<String>): List<Subject>
    fun create(name: String): Subject
}
