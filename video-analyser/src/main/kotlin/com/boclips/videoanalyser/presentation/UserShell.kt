package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.application.CreateVideosReport
import com.boclips.videoanalyser.application.RunSearchBenchmark
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class UserShell(private val createVideoReport: CreateVideosReport, private val runSearchBenchmark: RunSearchBenchmark) {

    @ShellMethod("Generate report with all faulty videos on Kaltura")
    fun faultyVideosKaltura(@ShellOption(help = "Specify filename of report") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.faultyVideosKaltura(filename, columns)
    }

    @ShellMethod("Generate report with all ready and pending videos hosted on Kaltura")
    fun nonErroredVideosOnKaltura(@ShellOption(help = "Specify filename of report") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.nonErroredVideosOnKaltura(filename, columns)
    }

    @ShellMethod("Generate report with all ready videos hosted on Kaltura")
    fun allVideosBoclips(@ShellOption(help = "Specify filename of report") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.allVideosBoclips(filename, columns)
    }

    @ShellMethod("Generate report of all unplayable videos (videos on Boclips but not playable on Kaltura)")
    fun unplayableVideos(@ShellOption(help = "Specify filename of report") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.unplayableVideos(filename, columns)
    }

    @ShellMethod("Generate report of all playable videos (videos available on Boclips, as well as Kaltura)")
    fun playableVideos(@ShellOption(help = "Please specify file name") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.playableVideos(filename, columns)
    }

    @ShellMethod("Generate report of all removable Kaltura videos (videos on Kaltura but not on Boclips)")
    fun freeableVideos(@ShellOption(help = "Please specify file name") filename: String, @ShellOption(help = "CSV columns") columns: String?) {
        createVideoReport.freeableVideos(filename, columns)
    }

    @ShellMethod("Count all videos on Boclips and/or Kaltura")
    fun countVideos(@ShellOption(help = "Specify source (kaltura, boclips, all)", defaultValue = "all") source: String) {
        when(source.toLowerCase()) {
            "kaltura" -> {
                println(createVideoReport.countKalturaVideos())
            }
            "boclips" -> {
                println(createVideoReport.countBoclipsVideos())
            }
            "all" -> {
                println(createVideoReport.countKalturaVideos())
                println(createVideoReport.countBoclipsVideos())
            }
        }
    }

    @ShellMethod("Search benchmark")
    fun searchBenchmark(@ShellOption(help = "Please specify search-query/video dataset file name ") filename: String) {
        println(runSearchBenchmark.runSearchBenchmark(filename))
    }

}
