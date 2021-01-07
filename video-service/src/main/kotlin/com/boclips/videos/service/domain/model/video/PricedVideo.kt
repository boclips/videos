package com.boclips.videos.service.domain.model.video

class PricedVideo(private val video: Video, val price: Price?) : BaseVideo by video
