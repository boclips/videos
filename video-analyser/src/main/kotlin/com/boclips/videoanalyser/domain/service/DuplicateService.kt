package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.DuplicateVideo

interface DuplicateService {
    fun getDuplicates(): Set<DuplicateVideo>
    fun deleteDuplicates()
}