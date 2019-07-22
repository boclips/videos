package com.boclips.videos.service.domain.model.subject

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun findById(id: SubjectId): Subject?
    fun findByIds(ids: Iterable<String>): List<Subject>
    fun findByName(name: String): Subject?
    fun create(name: String): Subject
    fun delete(id: SubjectId)
}
