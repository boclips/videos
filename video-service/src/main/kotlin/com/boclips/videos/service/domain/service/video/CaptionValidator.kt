package com.boclips.videos.service.domain.service.video

interface CaptionValidator{
    fun checkValid(content: String): Boolean
    fun parse(content: String): ArrayList<String>
}