package com.boclips.contentpartner.service.presentation

import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import org.springframework.hateoas.CollectionModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/distribution-methods")
class DistributionMethodsController : BaseController() {
    @GetMapping
    fun getDistributionMethods() =
        CollectionModel(listOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM))
}
