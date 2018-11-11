package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.application.RunSearchBenchmark
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class SearchBenchmark(private val runSearchBenchmark: RunSearchBenchmark) {

    @ShellMethod("Search benchmark")
    fun searchBenchmark(
            @ShellOption(help = "Please specify search-query/asset dataset file name ") expectationsFilename: String,
            @ShellOption(help = "Please specify output report file name ") outputFilename: String
    ) {
        runSearchBenchmark.execute(expectationsFilename, outputFilename)
    }

}
