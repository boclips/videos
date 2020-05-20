package com.boclips.contentpartner.service.presentation.distributionmethod

import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.DistributionMethodWrapper
import com.boclips.videos.api.response.channel.DistributionMethodsResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/distribution-methods")
class DistributionMethodsController  {
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
