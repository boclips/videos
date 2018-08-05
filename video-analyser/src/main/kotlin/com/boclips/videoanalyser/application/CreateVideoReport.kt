package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.application.csv.BoclipsVideoCsv
import com.boclips.videoanalyser.application.csv.CsvGenerator
import com.boclips.videoanalyser.domain.service.DuplicateService
import com.boclips.videoanalyser.domain.service.VideoAnalysisService
import org.springframework.stereotype.Service
import java.io.File

@Service
class CreateVideoReport(
        private val videoAnalysisService: VideoAnalysisService,
        private val duplicateService: DuplicateService
) {
    private val csvGenerator = CsvGenerator()

    fun faultyVideosKaltura(filename: String, columns: String?) {
        val videos = videoAnalysisService.getFaultyVideosFromKaltura().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, videos, columns)
    }

    fun nonErroredVideosOnKaltura(filename: String, columns: String?) {
        val readyAndPendingVideos = videoAnalysisService.getNonErrorVideosFromKaltura().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, readyAndPendingVideos, columns)
    }

    fun allVideosBoclips(filename: String, columns: String?) {
        val boclipsVideos = videoAnalysisService.getAllVideosFromBoclips().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, boclipsVideos, columns)
    }

    fun unplayableVideos(filename: String, columns: String?) {
        val unplayableVideos = videoAnalysisService.getUnplayableVideos().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, unplayableVideos, columns)
    }

    fun playableVideos(filename: String, columns: String?) {
        val playableVideos = videoAnalysisService.getPlayableVideos().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, playableVideos, columns)
    }

    fun freeableVideos(filename: String, columns: String?) {
        val freeableVideosIds = videoAnalysisService.getRemovableKalturaVideos().map { BoclipsVideoCsv.from(it) }
        writeVideosToFile(filename, freeableVideosIds, columns)
    }

    fun duplicates(filename: String, columns: String?) {
        val duplicates = duplicateService.getDuplicates().flatMap { duplicate ->
            listOf(
                    BoclipsVideoCsv.from(duplicate.originalVideo, "ORIGINAL"),
                    *(duplicate.duplicates.map { BoclipsVideoCsv.from(it, "DUPLICATES") }.toTypedArray())
            )
        }
        writeVideosToFile(filename, duplicates, columns)
    }

    fun countKalturaVideos(): String {
        val allKalturaVideos = videoAnalysisService.countAllKalturaVideos()
        return "Found $allKalturaVideos videos on Kaltura"
    }

    fun countBoclipsVideos(): String {
        val allBoclipsVideos = videoAnalysisService.countAllBoclipsVideos()
        return "Found $allBoclipsVideos videos in the Boclips database"
    }

    private fun writeVideosToFile(filename: String, videos: List<BoclipsVideoCsv>, columns: String?) {
        println("Writing report to file $filename")
        val file = File(filename)
        val headerColumns = columns?.split(",")?.toSet() ?: BoclipsVideoCsv.ALL_COLUMNS
        csvGenerator.writeCsv(file, videos, headerColumns)
        println("Success! ${file.absolutePath} contains ${videos.size} of videos")
    }
}
