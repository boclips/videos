package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.Video
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import com.boclips.videos.service.domain.service.SearchService
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/videos")
class VideoController(private val searchService: SearchService) {

    companion object {
        fun searchLink() = linkTo(methodOn(VideoController::class.java).search(null)).withRel("search")
    }

    @GetMapping
    fun search(@RequestParam("query") query: String?): Resources<Video> {
        val videos = searchService.search(query!!)
        return Resources(videos)
    }
}