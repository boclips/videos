package com.boclips.cleanser.application

import com.boclips.cleanser.domain.service.VideoAnalysisService
import com.boclips.cleanser.presentation.BoclipsVideoCsv
import org.springframework.stereotype.Service
import java.io.File

@Service
class CreateReport(private val videoAnalysisService: VideoAnalysisService) {
    private val csvGenerator = com.boclips.cleanser.presentation.CsvGenerator()

    fun unplayableVideos(filename: String) {
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()
        println("Writing unplayable videos to file $filename")
        val file = File(filename)
        csvGenerator.writeCsv(file, unplayableVideos.map { transform(it) })
        println("Success! ${file.absolutePath} contains ${unplayableVideos.size} of unplayable videos")
    }

    fun playableVideos(filename: String) {
        val playableVideos = videoAnalysisService.getPlayableVideos()
        println("Writing playable videos to file $filename")
        val file = File(filename)
        csvGenerator.writeCsv(file, playableVideos.map { transform(it) })
        println("Success! ${file.absolutePath} contains ${playableVideos.size} of playable videos")
    }

    fun freeableVideos(filename: String) {
        val freeableVideos = videoAnalysisService.getFreeableVideos()
        println("Writing freeable videos to file $filename")
        val file = File(filename)
        csvGenerator.writeCsv(file, freeableVideos.map { transform(it) })
        println("Success! ${file.absolutePath} contains ${freeableVideos.size} of freeable videos")
    }

    fun printCounts(source: String) {
        if ("kaltura" == source.toLowerCase() || "all" == source.toLowerCase()) {
            val allKalturaVideos = videoAnalysisService.countAllKalturaVideos()
            println("Found $allKalturaVideos videos on Kaltura")
        }
        if ("boclips" == source.toLowerCase() || "all" == source.toLowerCase()) {
            val allBoclipsVideos = videoAnalysisService.countAllBoclipsVideos()
            println("Found $allBoclipsVideos videos in the Boclips database")
        }
    }

    private fun transform(id: String): BoclipsVideoCsv {
        val boclipsVideoCsv = BoclipsVideoCsv()
        boclipsVideoCsv.id = id
        return boclipsVideoCsv
    }
}