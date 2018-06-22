package com.boclips.cleanser.domain.service

open class VideoAnalysisService(private val boclipsVideoService: BoclipsVideoService,
                           private val kalturaMediaService: KalturaMediaService) {
    fun getFaultyVideosFromKaltura(): Set<String> {
        return kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet()
    }

    fun getReadyVideosFromKaltura(): Set<String> {
        return kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
    }

    fun countAllKalturaVideos(): Long {
        return kalturaMediaService.countAllMediaEntries()
    }

    fun countAllBoclipsVideos(): Int {
        return boclipsVideoService.countAllVideos()
    }

    fun getAllVideosFromKaltura(): Set<String> {
        val readyVideos = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val faultyVideos = kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet()

        return readyVideos + faultyVideos
    }

    fun getUnplayableVideos(): Set<String> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map {it.referenceId}.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips - (readyVideosInKaltura + pendingVideosInKaltura)
    }

    fun getPlayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips.intersect(videosInKaltura)
    }

    fun getFreeableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosInKaltura - videosOnBoclips
    }
}
