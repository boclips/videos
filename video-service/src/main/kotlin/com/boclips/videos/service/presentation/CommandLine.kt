package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.application.channels.RebuildChannelIndex
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.search.RebuildSubjectIndex
import com.boclips.videos.service.application.video.GenerateTranscripts
import com.boclips.videos.service.application.video.UpdateYoutubePlayback
import com.boclips.videos.service.application.video.indexing.RebuildLegacySearchIndex
import com.boclips.videos.service.application.video.indexing.RebuildVideoIndex
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class CommandLine(
    val env: Environment,
    val collectionIndex: RebuildCollectionIndex,
    val videoIndex: RebuildVideoIndex,
    val subjectIndex: RebuildSubjectIndex,
    val channelIndex: RebuildChannelIndex,
    val rebuildLegacySearchIndex: RebuildLegacySearchIndex,
    val updateYoutubePlayback: UpdateYoutubePlayback,
    val generateTranscripts: GenerateTranscripts
) {
    companion object : KLogging()

    @Autowired
    lateinit var app: ApplicationContext

    @PostConstruct
    fun onBoot() {
        when (env.getProperty("mode")) {
            "reindex-collections" -> {
                collectionIndex.invoke(ConsoleProgressNotifier())
                System.exit(SpringApplication.exit(app))
            }
            "reindex-videos" -> {
                videoIndex.invoke(ConsoleProgressNotifier())
                System.exit(SpringApplication.exit(app))
            }
            "reindex-channels" -> {
                channelIndex.invoke(ConsoleProgressNotifier())
                System.exit(SpringApplication.exit(app))
            }
            "reindex-subjects" -> {
                subjectIndex.invoke(ConsoleProgressNotifier())
                System.exit(SpringApplication.exit(app))
            }
            "reindex-legacy" -> {
                rebuildLegacySearchIndex.invoke(ConsoleProgressNotifier())
                System.exit(SpringApplication.exit(app))
            }
            "synchronise-youtube-playback" -> {
                updateYoutubePlayback.invoke()
                System.exit(SpringApplication.exit(app))
            }
            "generate-transcripts" -> {
                generateTranscripts.invoke()
                System.exit(SpringApplication.exit(app))
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
