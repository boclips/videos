package com.boclips.cleanser.presentation

import com.boclips.cleanser.application.CreateReport
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class UserShell(private val createReport: CreateReport) {

    @ShellMethod("Generate report with all unplayable videos")
    fun unplayableVideos(@ShellOption(help = "Specify filename") filename: String) {
        println("Fetching unplayable videos...")
        createReport.unplayableVideos(filename)
        println("Finished job fetching unplayable videos")
    }

    @ShellMethod("Generate report with all playable videos")
    fun playableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all playable videos...")
        createReport.playableVideos(filename)
        println("Finished job fetching unplayable videos")
    }

    @ShellMethod("Generate report with all freeable Kaltura videos")
    fun freeableVideos(@ShellOption(help = "Please specify file name") filename: String) {
        println("Fetching all freeable videos...")
        createReport.freeableVideos(filename)
        println("Finished job fetching unplayable videos")
    }

    @ShellMethod("Count all videos")
    fun countVideos(@ShellOption(help = "Specify source (kaltura, boclips, all)", defaultValue = "all") source: String) {
        createReport.printCounts(source)
    }
}
