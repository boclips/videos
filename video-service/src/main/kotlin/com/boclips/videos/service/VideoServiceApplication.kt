package com.boclips.videos.service

import com.boclips.events.spring.EnableBoclipsEvents
import com.boclips.security.EnableBoclipsSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableBoclipsEvents(appName = "video-service")
@EnableAsync
@EnableSwagger2
@SpringBootApplication(exclude = [SolrAutoConfiguration::class])
class VideoServiceApplication

fun main(args: Array<String>) {
    runApplication<VideoServiceApplication>(*args)
}
