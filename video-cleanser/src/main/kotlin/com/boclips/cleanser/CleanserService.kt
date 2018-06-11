package com.boclips.cleanser

class CleanserService(val boclipsVideosRepository: BoclipsVideosRepository,
                      val kalturaVideosRepository: KalturaVideosRepository) {
    fun getNonPlayableVideos(): Set<Int> {
        val videosInKaltura = kalturaVideosRepository.getAllNonErroredVideos()
        val videosOnBoclips = boclipsVideosRepository.getAllIds()
        return videosOnBoclips - videosInKaltura
    }
}
