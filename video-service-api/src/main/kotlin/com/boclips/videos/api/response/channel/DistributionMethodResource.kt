package com.boclips.videos.api.response.channel

class DistributionMethodsResource(val _embedded: DistributionMethodWrapper)

class DistributionMethodWrapper(val distributionMethods: List<DistributionMethodResource>)

enum class DistributionMethodResource {
    DOWNLOAD,
    STREAM
}
