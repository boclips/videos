package com.boclips.videos.service.domain.service.subject

import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun findById(id: SubjectId): Subject?
    fun findByIds(ids: Iterable<String>): List<Subject>
    fun findByOrderedIds(ids: Iterable<String>): List<Subject>
    fun findByName(name: String): Subject?
    fun findByQuery(query: String): List<Subject>
    fun create(name: String): Subject
    fun delete(id: SubjectId)
    fun update(updateCommand: SubjectUpdateCommand): Subject
}
