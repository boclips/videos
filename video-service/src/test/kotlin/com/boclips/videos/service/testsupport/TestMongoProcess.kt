package com.boclips.videos.service.testsupport

import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import mu.KLogging
import org.springframework.boot.autoconfigure.mongo.MongoProperties

object TestMongoProcess {
    val process: MongodProcess by lazy {
        val starter = MongodStarter.getDefaultInstance()
        val host = "localhost"
        val port = MongoProperties.DEFAULT_PORT

        KLogging().logger.info { "Booting up MongoDB ${Version.Main.V3_6} on $host:$port" }

        val mongoConfig = MongodConfigBuilder()
            .version(Version.Main.V3_6)
            .net(Net(host, port, Network.localhostIsIPv6()))
            .build()

        val mongoExecutable = starter.prepare(mongoConfig)
        mongoExecutable.start()
    }
}