package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.RebuildSearchIndex
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/actions")
class AdminController(
        private val rebuildSearchIndex: RebuildSearchIndex,
        private val buildLegacySearchIndex: BuildLegacySearchIndex
) {
    @PostMapping("/rebuild_search_index")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun rebuildSearchIndex() {
        rebuildSearchIndex.execute()
    }

    @PostMapping("/build_legacy_search_index")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun buildLegacySearchIndex() {
        buildLegacySearchIndex.execute()
    }
}