package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.domain.model.DuplicateVideo

interface VideoRemapperService {
    fun remapBasketsPlaylistsAndCollections(duplicateVideo: DuplicateVideo)
    fun disableIndexesBeforeRemapping()
    fun enableIndexesAfterRemapping()
}