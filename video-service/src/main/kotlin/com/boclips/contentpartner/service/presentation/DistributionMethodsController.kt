package com.boclips.contentpartner.service.presentation

import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/distribution-methods")
class DistributionMethodsController : BaseController() {
    @GetMapping
    fun getDistributionMethods() =
        Resources(listOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM))
}
