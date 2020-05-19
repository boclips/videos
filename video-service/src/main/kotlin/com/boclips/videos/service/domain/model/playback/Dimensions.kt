package com.boclips.videos.service.domain.model.playback

data class Dimensions(val width: Int, val height: Int) {
    fun isFHD() = height >= 1080
    fun isHD() = height >= 720
}
