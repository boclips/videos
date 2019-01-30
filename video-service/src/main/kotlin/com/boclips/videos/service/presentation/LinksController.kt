package com.boclips.videos.service.presentation

import org.springframework.hateoas.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {

    @GetMapping
    fun search(): Resource<String> = Resource("",
            VideoController.getSearchLink(),
            VideoController.getVideoLink(),
            VideoController.getVideosLink(),
            EventController.createPlaybackEventLink(),
            EventController.createNoResultsEventLink(),
            CollectionsController.getUserDefaultCollectionLink().withRel("userDefaultCollection")
    )
}
