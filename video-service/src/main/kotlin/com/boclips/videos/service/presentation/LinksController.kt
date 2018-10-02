package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.event.EventController
import com.boclips.videos.service.presentation.user.UserController
import com.boclips.videos.service.presentation.video.VideoController
import org.springframework.hateoas.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {

    @GetMapping
    fun search(): Resource<String> = Resource("",
            VideoController.searchLink(),
            VideoController.getVideoLink(),
            EventController.createEventLink(),
            UserController.profileLink()
    )
}
