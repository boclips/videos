package com.boclips.cleanser.infrastructure

import com.boclips.cleanser.domain.CleanserService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.KalturaVideosRepository

class CleanserServiceImpl(val boclipsVideosRepository: BoclipsVideosRepository,
                          val kalturaVideosRepository: KalturaVideosRepository) : CleanserService {
    override fun getNonPlayableVideos(): Set<String> {
        val videosInKaltura = kalturaVideosRepository.getAllIds()
        val videosOnBoclips = boclipsVideosRepository.getAllLegacyIds().map { it.toString() }.toSet()
        return videosOnBoclips - videosInKaltura
    }
}
