package com.boclips.videos.service.infrastructure.video.mysql

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.EntityManagerFactory

internal class VideoSequenceReaderTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var autowiredEntityManagerFactory: EntityManagerFactory

    lateinit var videoSequenceReader: VideoSequenceReader

    @BeforeEach
    fun setup() {
        val h2CompatibleFetchSize = 100
        videoSequenceReader = VideoSequenceReader(fetchSize = h2CompatibleFetchSize)
                .apply {
                    entityManagerFactory = autowiredEntityManagerFactory
                }
    }

    @Test
    fun `streams all the videos`() {
        saveVideo(title = "Title 1")
        saveVideo(title = "Title 2")
        saveVideo(title = "Title 3")

        var titles = emptyList<String>()

        videoSequenceReader.readOnly { sequence ->
            titles = sequence.mapNotNull { it.title }.toList()
        }

        assertThat(titles).containsExactly("Title 1", "Title 2", "Title 3")
    }
}