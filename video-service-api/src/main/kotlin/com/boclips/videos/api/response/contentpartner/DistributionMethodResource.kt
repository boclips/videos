package com.boclips.videos.api.response.contentpartner

class DistributionMethodsResource(val _embedded: DistributionMethodWrapper)

class DistributionMethodWrapper(val distributionMethods: List<DistributionMethodResource>)

enum class DistributionMethodResource {
    DOWNLOAD,
    STREAM
}
