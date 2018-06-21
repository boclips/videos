package com.boclips.cleanser.presentation

import com.boclips.cleanser.domain.service.VideoAnalysisService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class UserShell(private val videoAnalysisService: VideoAnalysisService) {

    @ShellMethod("Generate report with all unplayable videos")
    fun unplayableVideos(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching unplayable videos...")
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()
        println("Generated file $filename with ${unplayableVideos.size} unplayable videos")
    }

    @ShellMethod("Generate report with all playable videos")
    fun playableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all playable videos...")
        val playableVideos = videoAnalysisService.getPlayableVideos()
        println("Generated file $filename with ${playableVideos.size} unplayable videos")
    }

    @ShellMethod("Generate report with all freeable Kaltura videos")
    fun freeableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all freeable videos...")
        val playableVideos = videoAnalysisService.getFreeableVideos()
        println("Generated file $filename with ${playableVideos.size} unplayable videos")
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
}
