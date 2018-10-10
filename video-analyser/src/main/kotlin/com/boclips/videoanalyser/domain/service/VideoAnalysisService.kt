package com.boclips.videoanalyser.domain.service

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.KalturaVideo
import com.boclips.videoanalyser.domain.model.PlayableVideo

open class VideoAnalysisService(private val boclipsVideoService: BoclipsVideoService,
                                private val kalturaMediaService: KalturaMediaService) {
    fun countAllKalturaVideos(): Long {
        return kalturaMediaService.countAllMediaEntries()
    }

    fun countAllBoclipsVideos(): Int {
        return boclipsVideoService.countAllVideos()
    }

    fun getFaultyVideosFromKaltura(): Set<BoclipsVideo> {
        return boclipsVideoService.getVideoMetadataByReferenceIds(kalturaMediaService.getFaultyMediaEntries().map { it.referenceId }.toSet())
    }

    fun getNonErrorVideosFromKaltura(): Set<BoclipsVideo> {
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        return boclipsVideoService.getVideoMetadataByReferenceIds(pendingVideosInKaltura + readyVideosInKaltura)
    }

    fun getAllVideosFromBoclips(): Set<BoclipsVideo> {
        return boclipsVideoService.getVideoMetadataByReferenceIds(boclipsVideoService.getAllVideos().map { it.referenceId }.toSet())
    }

    fun getPlayableVideos(): Set<PlayableVideo> {
        val kalturaVideos = kalturaMediaService.getReadyMediaEntries()
        val videoIdsInKaltura = kalturaVideos.map { it.referenceId }.toSet()

        val boclipsVideos = boclipsVideoService.getAllVideos()
        val videoIdsOnBoclips = boclipsVideos.map { it.referenceId }.toSet()

        val intersectionBoclipsKaltura = videoIdsOnBoclips.intersect(videoIdsInKaltura)
        val enrichedPlayableVideos = boclipsVideoService.getVideoMetadataByReferenceIds(intersectionBoclipsKaltura)

        return enrichedPlayableVideos.map { boclipsVideo: BoclipsVideo ->
            val kalturaVideo = kalturaVideos.first { it.referenceId == boclipsVideo.referenceId }
            PlayableVideo(boclipsVideo, kalturaVideo)
        }.toSet()
    }

    fun getUnplayableVideos(): Set<BoclipsVideo> {
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.referenceId }.toSet()
        val pendingVideosInKaltura = kalturaMediaService.getPendingMediaEntries().map { it.referenceId }.toSet()
        val readyVideosInKaltura = kalturaMediaService.getReadyMediaEntries().map { it.referenceId }.toSet()
        return boclipsVideoService.getVideoMetadataByReferenceIds(videosOnBoclips - (readyVideosInKaltura + pendingVideosInKaltura))
    }

    fun getRemovableKalturaVideos(): Set<KalturaVideo> {
        val videosInKaltura = kalturaMediaService.getReadyMediaEntries()
        val videosOnBoclips = boclipsVideoService.getAllVideos().map { it.referenceId }.toSet()

        return videosInKaltura.filter { !videosOnBoclips.contains(it.referenceId) }.toSet()
    }
}
