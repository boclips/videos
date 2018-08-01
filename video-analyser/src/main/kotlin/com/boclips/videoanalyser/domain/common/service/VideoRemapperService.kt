package com.boclips.videoanalyser.domain.common.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate

interface VideoRemapperService {
    fun remapBasketsPlaylistsAndCollections(duplicate: Duplicate)
}