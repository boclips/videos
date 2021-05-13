package com.boclips.videos.api.response.video

import com.boclips.videos.api.response.channel.TaxonomyCategoryResource

data class VideoTaxonomyResourceWrapper(
    val channel: VideoTaxonomyResource,
    val manual: VideoTaxonomyResource
)

data class VideoTaxonomyResource(
    val categories: List<TaxonomyCategoryResource>? = null,
)
