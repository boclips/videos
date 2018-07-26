package com.boclips.mysql2es

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [ElasticsearchAutoConfiguration::class, ElasticsearchDataAutoConfiguration::class])
class Mysql2esApplication {

}

fun main(args: Array<String>) {
    runApplication<Mysql2esApplication>(*args)
}
