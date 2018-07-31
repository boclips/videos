package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate

interface DuplicateService {
    fun getDuplicates(): Set<Duplicate>
}