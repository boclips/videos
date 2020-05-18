package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId

interface ContentWarningRepository {
    fun findById(id: ContentWarningId): ContentWarning?
    fun findAll(): List<ContentWarning>
    fun create(label: String): ContentWarning
}
