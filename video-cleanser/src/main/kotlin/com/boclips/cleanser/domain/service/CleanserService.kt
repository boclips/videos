package com.boclips.cleanser.domain.service

class CleanserService(private val boclipsVideoService: BoclipsVideoService,
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

    fun getAllVideosFromKaltura(): Set<String> {
        val readyVideos = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val faultyVideos = kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet()

        return readyVideos + faultyVideos
    }

    fun getUnplayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }
        val videosOnBoclips = boclipsVideoService.getAllPublishedVideos().map { it }.toSet()
        return videosOnBoclips - videosInKaltura
    }

    fun getPlayableVideos(): Set<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }
        val videosOnBoclips = boclipsVideoService.getAllPublishedVideos().map { it }.toSet()
        return videosOnBoclips.intersect(videosInKaltura)
    }

    fun getFreeableVideos(): List<String> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }
        val videosOnBoclips = boclipsVideoService.getAllPublishedVideos().map { it }.toSet()
        return videosInKaltura - videosOnBoclips
    }
}
