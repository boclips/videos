package com.boclips

import com.boclips.eventbus.EnableBoclipsEvents
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableBoclipsEvents
@EnableCaching
@SpringBootApplication(
    exclude = [SolrAutoConfiguration::class],
    scanBasePackages = ["com.boclips.videos", "com.boclips.contentpartner"]
)
class VideoServiceApplication

fun main(args: Array<String>) {
    runApplication<VideoServiceApplication>(*args)
}
