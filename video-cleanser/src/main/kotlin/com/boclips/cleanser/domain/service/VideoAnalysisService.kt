package com.boclips.cleanser.domain.service

open class VideoAnalysisService(private val boclipsVideoService: BoclipsVideoService,
                                private val kalturaMediaService: KalturaMediaService) {
    fun countAllKalturaVideos(): Long {
        return kalturaMediaService.countAllMediaEntries()
    }

    fun countAllBoclipsVideos(): Int {
        return boclipsVideoService.countAllVideos()
    }

    fun getFaultyVideosFromKaltura(): Set<String> {
        return kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet()
    }

    fun getNonErrorVideosFromKaltura(): Set<String> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        return pendingVideosInKaltura + readyVideosInKaltura
    }

    fun getAllVideosFromBoclips(): Set<String> {
        return boclipsVideoService.getAllVideos().map { it.id }.toSet()
    }

    fun getPlayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips.intersect(videosInKaltura)
    }

    fun getUnplayableVideos(): Set<String> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosOnBoclips - (readyVideosInKaltura + pendingVideosInKaltura)
    }

    fun getFreeableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.id }.toSet()
        return videosInKaltura - videosOnBoclips
    }
}
