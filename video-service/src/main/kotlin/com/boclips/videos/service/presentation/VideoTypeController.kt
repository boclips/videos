package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.videos.model.VideoType
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/video-types")
class VideoTypeController : BaseController() {
    @GetMapping
    fun videoTypes(): Resources<*> {
        return Resources(
            VideoType.values().toList()
        )
    }
}
