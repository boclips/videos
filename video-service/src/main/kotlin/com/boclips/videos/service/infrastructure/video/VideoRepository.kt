package com.boclips.videos.service.infrastructure.video

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VideoRepository : CrudRepository<VideoEntity, Long>