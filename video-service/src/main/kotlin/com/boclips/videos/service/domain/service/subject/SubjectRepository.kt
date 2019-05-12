package com.boclips.videos.service.domain.service.subject

import com.boclips.videos.service.domain.model.collection.Subject

interface SubjectRepository {
    fun findAll(): List<Subject>
    fun create(name: String): Subject
}
