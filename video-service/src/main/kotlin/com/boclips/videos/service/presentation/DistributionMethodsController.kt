package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/distribution-methods")
class DistributionMethodsController {
    @GetMapping
    fun getDistributionMethods() =
        Resources(listOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM))
}