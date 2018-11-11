package com.boclips.videos.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableAsync
@EnableSwagger2
@SpringBootApplication
class VideoServiceApplication

fun main(args: Array<String>) {
    runApplication<VideoServiceApplication>(*args)
}
