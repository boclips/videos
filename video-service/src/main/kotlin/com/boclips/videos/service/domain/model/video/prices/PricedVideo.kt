package com.boclips.videos.service.domain.model.video.prices

import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.Price
import com.boclips.videos.service.domain.model.video.Video

class PricedVideo(private val video: Video, val price: Price?) : BaseVideo by video
