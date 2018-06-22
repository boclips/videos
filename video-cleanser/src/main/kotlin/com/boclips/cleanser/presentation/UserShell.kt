package com.boclips.cleanser.presentation

import com.boclips.cleanser.domain.service.VideoAnalysisService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File

@ShellComponent
class UserShell(private val videoAnalysisService: VideoAnalysisService) {
    private val csvGenerator = com.boclips.cleanser.presentation.CsvGenerator()

    @ShellMethod("Generate report with all faulty videos on Kaltura")
    fun faultyVideosKaltura(@ShellOption(help = "Specify filename of report") filename: String) {
        val videos = videoAnalysisService.getFaultyVideosFromKaltura().map { transform(it) }
        writeVideosToFile(filename, videos)
    }

    @ShellMethod("Generate report with all ready and pending videos hosted on Kaltura")
    fun nonErroredVideosOnKaltura(@ShellOption(help = "Specify filename of report") filename: String) {
        val readyAndPendingVideos = videoAnalysisService.getNonErrorVideosFromKaltura().map { transform(it) }
        writeVideosToFile(filename, readyAndPendingVideos)
    }

    @ShellMethod("Generate report with all ready videos hosted on Kaltura")
    fun allVideosBoclips(@ShellOption(help = "Specify filename of report") filename: String) {
        val boclipsVideos = videoAnalysisService.getAllVideosFromBoclips().map { transform(it) }
        writeVideosToFile(filename, boclipsVideos)
    }

    @ShellMethod("Generate report of all unplayable videos (videos on Boclips but not playable on Kaltura)")
    fun unplayableVideos(@ShellOption(help = "Specify filename of report") filename: String) {
        val unplayableVideos = videoAnalysisService.getUnplayableVideos().map { transform(it) }
        writeVideosToFile(filename, unplayableVideos)
    }

    @ShellMethod("Generate report of all playable videos (videos available on Boclips, as well as Kaltura)")
    fun playableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        val playableVideos = videoAnalysisService.getPlayableVideos().map { transform(it) }
        writeVideosToFile(filename, playableVideos)
    }

    @ShellMethod("Generate report of all removable Kaltura videos (videos on Kaltura but not on Boclips)")
    fun freeableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        val freeableVideosIds = videoAnalysisService.getRemovableKalturaVideos().map { transform(it) }
        writeVideosToFile(filename, freeableVideosIds)
    }

    @ShellMethod("Count all videos on Boclips and/or Kaltura")
    fun countVideos(@ShellOption(help = "Specify source (kaltura, boclips, all)", defaultValue = "all") source: String) {
        if ("kaltura" == source.toLowerCase() || "all" == source.toLowerCase()) {
            val allKalturaVideos = videoAnalysisService.countAllKalturaVideos()
            println("Found $allKalturaVideos videos on Kaltura")
        }
        if ("boclips" == source.toLowerCase() || "all" == source.toLowerCase()) {
            val allBoclipsVideos = videoAnalysisService.countAllBoclipsVideos()
            println("Found $allBoclipsVideos videos in the Boclips database")
        }
    }

    private fun writeVideosToFile(filename: String, videos: List<BoclipsVideoCsv>) {
        println("Writing report to file $filename")
        val file = File(filename)
        csvGenerator.writeCsv(file, videos)
        println("Success! ${file.absolutePath} contains ${videos.size} of videos")
    }

    private fun transform(id: String): BoclipsVideoCsv {
        val boclipsVideoCsv = BoclipsVideoCsv()
        boclipsVideoCsv.id = id
        return boclipsVideoCsv
    }
}
