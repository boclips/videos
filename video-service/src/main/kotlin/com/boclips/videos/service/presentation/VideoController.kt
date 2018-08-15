package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.Video
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import com.boclips.videos.service.domain.SearchService
import org.springframework.web.bind.annotation.RestController


@RestController("v1/videos")
class VideoController(private val searchService: SearchService) {

    @GetMapping
    fun search(@RequestParam("query") query: String): Resources<Video> {
        val videos = searchService.search(query)
        return Resources(videos)
    }
}