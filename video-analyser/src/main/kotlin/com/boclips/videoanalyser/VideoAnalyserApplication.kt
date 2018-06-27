package com.boclips.videoanalyser

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VideoAnalyserApplication

fun main(args: Array<String>) {
    runApplication<VideoAnalyserApplication>(*args)
}
