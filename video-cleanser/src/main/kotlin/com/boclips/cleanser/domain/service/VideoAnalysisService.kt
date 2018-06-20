package com.boclips.cleanser.domain.service

class VideoAnalysisService(private val boclipsVideoService: BoclipsVideoService,
                           private val kalturaMediaService: KalturaMediaService) {
    fun getFaultyVideosFromKaltura(): Set<String> {
        return kalturaMediaService.getFaultyMediaEntries()
    }

    fun getReadyVideosFromKaltura(): Set<String> {
        return kalturaMediaService.getReadyMediaEntries()
    }

    fun countAllKalturaVideos(): Long {
        return kalturaMediaService.countAllMediaEntries()
    }

    fun countAllBoclipsVideos(): Int {
        return boclipsVideoService.countAllVideos()
    }

    fun getAllVideosFromKaltura(): Set<String> {
        val readyVideos = kalturaMediaService.getReadyMediaEntries()
        val faultyVideos = kalturaMediaService.getFaultyMediaEntries()

        return readyVideos + faultyVideos
    }

    fun getUnplayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips - videosInKaltura
    }

    fun getPlayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips.intersect(videosInKaltura)
    }

    fun getFreeableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosInKaltura - videosOnBoclips
    }
}
