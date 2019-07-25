package com.boclips.videos.service.domain.model.tag

interface TagRepository {
    fun findAll(): List<Tag>
    fun findById(id: TagId): Tag?
    fun findByIds(ids: Iterable<String>): List<Tag>
    fun findByLabel(label: String): Tag?
    fun create(label: String): Tag
    fun delete(id: TagId)
}
