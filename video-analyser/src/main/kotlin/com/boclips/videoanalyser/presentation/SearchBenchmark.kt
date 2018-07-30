package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.application.RunSearchBenchmark
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class SearchBenchmark(private val runSearchBenchmark: RunSearchBenchmark) {

    @ShellMethod("Search benchmark")
    fun searchBenchmark(@ShellOption(help = "Please specify search-query/video dataset file name ") filename: String) {
        println(runSearchBenchmark.runSearchBenchmark(filename))
    }

}
