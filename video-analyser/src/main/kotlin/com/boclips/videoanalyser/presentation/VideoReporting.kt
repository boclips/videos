package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.application.CreateVideoReport
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class VideoReporting(private val videoReport: CreateVideoReport) {

    @ShellMethod("Generate report with all faulty videos on Kaltura")
    fun faultyVideosKaltura(
            @ShellOption(help = "Specify filename of report") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.faultyVideosKaltura(filename, columns)
    }

    @ShellMethod("Generate report with all ready and pending videos hosted on Kaltura")
    fun nonErroredVideosOnKaltura(
            @ShellOption(help = "Specify filename of report") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.nonErroredVideosOnKaltura(filename, columns)
    }

    @ShellMethod("Generate report with all ready videos hosted on Kaltura")
    fun allVideosBoclips(
            @ShellOption(help = "Specify filename of report") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.allVideosBoclips(filename, columns)
    }

    @ShellMethod("Generate report of all unplayable videos, aka VPCS (videos on Boclips but not playable on Kaltura)")
    fun unplayableVideos(
            @ShellOption(help = "Specify filename of report") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.unplayableVideos(filename, columns)
    }

    @ShellMethod("Generate report of all playable videos (videos available on Boclips, as well as Kaltura)")
    fun playableVideos(
            @ShellOption(help = "Please specify file name") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.playableVideos(filename, columns)
    }

    @ShellMethod("Generate report of all removable Kaltura videos (videos on Kaltura but not on Boclips)")
    fun freeableVideos(
            @ShellOption(help = "Please specify file name") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.freeableVideos(filename, columns)
    }

    @ShellMethod("Generate report of soft duplicates")
    fun duplicates(
            @ShellOption(help = "Please specify file name") filename: String,
            @ShellOption(help = "CSV columns", defaultValue = ShellOption.NULL) columns: String?) {
        videoReport.duplicates(filename, columns)
    }

    @ShellMethod("Count all videos on Boclips and/or Kaltura")
    fun countVideos(@ShellOption(help = "Specify source (kaltura, boclips, all)", defaultValue = "all") source: String) {
        when (source.toLowerCase()) {
            "kaltura" -> {
                println(videoReport.countKalturaVideos())
            }
            "boclips" -> {
                println(videoReport.countBoclipsVideos())
            }
            "all" -> {
                println(videoReport.countKalturaVideos())
                println(videoReport.countBoclipsVideos())
            }
        }
    }
}
