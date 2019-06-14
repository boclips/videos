package com.boclips.videos.service.domain.model.disciplines

interface DisciplineRepository {
    fun findAll(): List<Discipline>
    fun findOne(id: String): Discipline?
    fun findByIds(ids: Iterable<String>): List<Discipline>
    fun create(code: String, name: String): Discipline
    fun update(discipline: Discipline)
}
