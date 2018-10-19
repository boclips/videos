package com.boclips.videos.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class VideoServiceApplication

fun main(args: Array<String>) {
    runApplication<VideoServiceApplication>(*args)
}
