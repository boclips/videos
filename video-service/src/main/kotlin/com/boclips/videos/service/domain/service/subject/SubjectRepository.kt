package com.boclips.videos.service.domain.service.subject

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun findById(id: SubjectId): Subject?
    fun findByIds(ids: Iterable<String>): List<Subject>
    fun findByName(name: String): Subject?
    fun create(name: String): Subject
    fun delete(id: SubjectId)
    fun updateName(subjectId: SubjectId, name: String)
}
