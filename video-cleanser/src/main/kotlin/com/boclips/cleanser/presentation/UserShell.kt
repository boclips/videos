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
    fun faultyVideosKaltura(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching faulty videos on Kaltura...")
        val unplayableVideos = videoAnalysisService.getFaultyVideosFromKaltura().map { transform(it) }
        writeVideosToFile(filename, unplayableVideos)
        println("Finished job fetching faulty videos on Kaltura")
    }

    @ShellMethod("Generate report with all non-errored videos on Kaltura")
    fun nonErroredVideosOnKaltura(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching non-errored videos on Kaltura...")
        val readyAndPendingVideos = videoAnalysisService.getNonErrorVideosFromKaltura().map { transform(it) }
        writeVideosToFile(filename, readyAndPendingVideos)
        println("Finished job fetching non-errored videos on Kaltura")
    }

    @ShellMethod("Generate report with all ready videos on Kaltura")
    fun allVideosBoclips(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching ready videos on Boclips...")
        val boclipsVideos = videoAnalysisService.getAllVideosFromBoclips().map { transform(it) }
        writeVideosToFile(filename, boclipsVideos)
        println("Finished job fetching all videos on Boclips")
    }

    @ShellMethod("Generate report with all unplayable videos")
    fun unplayableVideos(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching unplayable videos...")
        val unplayableVideos = videoAnalysisService.getUnplayableVideos().map { transform(it) }
        writeVideosToFile(filename, unplayableVideos)
        println("Finished job fetching unplayable videos")
    }

    @ShellMethod("Generate report with all playable videos")
    fun playableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all playable videos...")
        val playableVideos = videoAnalysisService.getPlayableVideos().map { transform(it) }
        writeVideosToFile(filename, playableVideos)
        println("Finished job fetching playable videos")
    }

    @ShellMethod("Generate report with all freeable Kaltura videos")
    fun freeableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all freeable videos...")
        val freeableVideosIds = videoAnalysisService.getFreeableVideos().map { transform(it) }
        writeVideosToFile(filename, freeableVideosIds)
        println("Finished job fetching freeable videos")
    }

    @ShellMethod("Count all videos")
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
        println("Writing videos to file $filename")
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
