package com.boclips.videos.service.domain.model.tag

interface TagRepository {
    fun findAll(): List<Tag>
    fun findById(id: TagId): Tag?
    fun findByIds(ids: Iterable<String>): List<Tag>
    fun findByName(name: String): Tag?
    fun create(name: String): Tag
    fun delete(id: TagId)
}
