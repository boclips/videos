package com.boclips.contentpartner.service.presentation

import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.response.contentpartner.DistributionMethodWrapper
import com.boclips.videos.api.response.contentpartner.DistributionMethodsResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/distribution-methods")
class DistributionMethodsController : BaseController() {
    @GetMapping
    fun getDistributionMethods(): DistributionMethodsResource {
        return DistributionMethodsResource(
            _embedded = DistributionMethodWrapper(
                listOf(
                    DistributionMethodResource.DOWNLOAD,
                    DistributionMethodResource.STREAM
                )
            )
        )
    }
}
