package com.boclips.videos.service.infrastructure.video.mysql

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VideoEntityRepository : CrudRepository<VideoEntity, Long> {
    fun countBySourceAndUniqueId(contentPartnerId: String, partnerVideoId: String): Int

    fun findAllByIdIn(ids: List<Long>): List<VideoEntity>

    @Modifying
    @Query("update com.boclips.videos.service.infrastructure.video.mysql.VideoEntity v set v.searchable = ?1 where v.id in ?2")
    fun setSearchableByIdIn(searchable: Boolean, id: List<Long>)

    @Query("select v from com.boclips.videos.service.infrastructure.video.mysql.VideoEntity v where v.id in ?1")
    fun get1(id: List<Long>): VideoEntity
}