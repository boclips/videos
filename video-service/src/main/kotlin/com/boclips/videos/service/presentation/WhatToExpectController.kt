package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.contentpartner.WhatToExpectResource
import com.boclips.videos.api.response.contentpartner.WhatToExpectWrapperResource
import com.boclips.videos.service.domain.model.video.WhatToExpect
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/what-to-expect")
class WhatToExpectController(
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride
) : BaseController(accessRuleService, getUserIdOverride) {
    @GetMapping
    fun whatToExpect(): WhatToExpectResource {
        return WhatToExpectResource(
            _embedded = WhatToExpectWrapperResource(
                whatToExpect = WhatToExpect.values().map { it.value }.toList()
            )
        )
    }
}