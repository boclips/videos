package com.boclips.videos.service.presentation.hateoas

import com.boclips.search.service.domain.videos.model.VideoType
import org.springframework.hateoas.RelProvider
import org.springframework.stereotype.Component

@Component
class VideoTypeRelProvider : RelProvider {
    override fun getItemResourceRelFor(type: Class<*>?) = "videoType"

    override fun supports(clazz: Class<*>?) = clazz == VideoType::class.java

    override fun getCollectionResourceRelFor(type: Class<*>?) = "videoTypes"
}