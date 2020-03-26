package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.api.response.video.VideoTypesResource
import com.boclips.videos.api.response.video.VideoTypesWrapperResource
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/video-types")
class VideoTypeController(
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride
) : BaseController(accessRuleService, getUserIdOverride) {
    @GetMapping
    fun videoTypes(): VideoTypesResource {
        return VideoTypesResource(
            _embedded = VideoTypesWrapperResource(
                videoTypes = VideoType.values().map { it.name }.toList()
            )
        )
    }
}
