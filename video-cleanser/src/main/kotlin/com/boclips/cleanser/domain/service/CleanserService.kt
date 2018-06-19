package com.boclips.cleanser.domain.service

import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository

class CleanserService(val boclipsVideosRepository: BoclipsVideosRepository,
                      val kalturaMediaService: KalturaMediaService) {
    fun getUnplayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }
        val videosOnBoclips = boclipsVideosRepository.getAllPublishedVideos().map { it.toString() }.toSet()
        return videosOnBoclips - videosInKaltura
    }
}
