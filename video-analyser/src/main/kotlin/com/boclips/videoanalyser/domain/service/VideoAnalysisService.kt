package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import com.boclips.videoanalyser.presentation.BoclipsVideoCsv

open class VideoAnalysisService(private val boclipsVideoService: BoclipsVideoService,
                                private val kalturaMediaService: KalturaMediaService) {
    fun countAllKalturaVideos(): Long {
        return kalturaMediaService.countAllMediaEntries()
    }

    fun countAllBoclipsVideos(): Int {
        return boclipsVideoService.countAllVideos()
    }

    fun getFaultyVideosFromKaltura(): Set<BoclipsVideo> {
        return boclipsVideoService.getVideoMetadata(kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet())
    }

    fun getNonErrorVideosFromKaltura(): Set<BoclipsVideo> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        return boclipsVideoService.getVideoMetadata(pendingVideosInKaltura + readyVideosInKaltura)
    }

    fun getAllVideosFromBoclips(): Set<BoclipsVideo> {
        return boclipsVideoService.getVideoMetadata(boclipsVideoService.getAllVideos().map { it.kalturaReferenceId() }.toSet())
    }

    fun getPlayableVideos(): Set<BoclipsVideo> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.kalturaReferenceId() }.toSet()
        return boclipsVideoService.getVideoMetadata(videosOnBoclips.intersect(videosInKaltura))
    }

    fun getUnplayableVideos(): Set<BoclipsVideo> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.kalturaReferenceId() }.toSet()
        return boclipsVideoService.getVideoMetadata(videosOnBoclips - (readyVideosInKaltura + pendingVideosInKaltura))
    }

    fun getRemovableKalturaVideos(): Set<KalturaVideo> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.kalturaReferenceId() }.toSet()

        return videosInKaltura.filter { !videosOnBoclips.contains(it.referenceId) }.toSet()
    }
}
