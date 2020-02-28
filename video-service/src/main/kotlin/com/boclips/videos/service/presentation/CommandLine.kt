package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.video.indexing.RebuildVideoIndex
import mu.KLogging
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

@Component
class CommandLine(
    val env: Environment,
    val collectionIndex: RebuildCollectionIndex,
    val videoIndex: RebuildVideoIndex
) {
    companion object : KLogging()

    @PostConstruct
    fun onBoot() {
        when (env.getProperty("mode")) {
            "reindex-collections" -> {
                collectionIndex.invoke(ConsoleProgressNotifier())
                exitProcess(0)
            }
            "reindex-videos" -> {
                videoIndex.invoke(ConsoleProgressNotifier())
                exitProcess(0)
            }
        }
    }
}

class ConsoleProgressNotifier : ProgressNotifier {
    companion object : KLogging()

    override fun send(message: String) {
        logger.info(message)
    }
}