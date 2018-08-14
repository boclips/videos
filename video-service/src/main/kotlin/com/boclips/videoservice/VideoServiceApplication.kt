package com.boclips.videoservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VideoServiceApplication

fun main(args: Array<String>) {
    runApplication<VideoServiceApplication>(*args)
}
