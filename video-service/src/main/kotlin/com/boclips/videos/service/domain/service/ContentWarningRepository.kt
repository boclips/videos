package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.contentwarning.ContentWarning

interface ContentWarningRepository {
    fun findAll(): List<ContentWarning>
    fun create(label: String): ContentWarning
}
