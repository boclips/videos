package com.boclips.videos.service.domain.model.video.prices

import com.boclips.videos.service.domain.model.user.OrganisationsPrices
import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.Video

class VideoWithPrices(private val video: Video, val prices: OrganisationsPrices): BaseVideo by video
