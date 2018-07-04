package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.MetadataTestRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.Month

class BoclipsVideoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var boclipsVideoRepository: BoclipsVideoRepository

    @Autowired
    lateinit var metadataTestRepository: MetadataTestRepository

    @Test
    fun getAllIds() {
        metadataTestRepository.insert(id = "1", title = "great title", contentProvider = "Bloomie")
        metadataTestRepository.insert(id = "2")

        assertThat(boclipsVideoRepository.getAllVideos().first().id).isEqualTo("1")
        assertThat(boclipsVideoRepository.getAllVideos().first().title).isEqualTo("great title")
        assertThat(boclipsVideoRepository.getAllVideos().first().contentProvider).isEqualTo("Bloomie")
    }

    @Test
    fun countAllVideos() {
        assertThat(boclipsVideoRepository.countAllVideos()).isEqualTo(0)

        metadataTestRepository.insert(id = "1")
        metadataTestRepository.insert(id = "2")

        assertThat(boclipsVideoRepository.countAllVideos()).isEqualTo(2)
    }

    @Test
    fun getVideoMetadata_whenNoReferenceId() {
        metadataTestRepository.insert(
                id = "1",
                title = "great title",
                contentProvider = "Bloomie",
                contentProviderId = "b1",
                description = "desc",
                duration = "01:02:03",
                date = LocalDate.of(2018, Month.JUNE, 10).atStartOfDay()
        )
        metadataTestRepository.insert(id = "2", title = null, contentProvider = null, contentProviderId = null)
        metadataTestRepository.insert(id = "3", title = null, contentProvider = null, contentProviderId = null)

        assertThat(boclipsVideoRepository.getVideoMetadata(setOf("1", "2"))).containsExactly(
                BoclipsVideo(
                        id = "1",
                        title = "great title",
                        contentProvider = "Bloomie",
                        contentProviderId = "b1",
                        duration = "01:02:03",
                        date = LocalDate.of(2018, Month.JUNE, 10).atStartOfDay(),
                        description = "desc"
                ),
                BoclipsVideo(id = "2")
        )
    }

    @Test
    fun getVideoMetadata_whenReferenceIdPresent() {
        metadataTestRepository.insert(
                id = "1",
                referenceId = "reference-id-1",
                title = "great title",
                contentProvider = "Bloomie",
                contentProviderId = "b1",
                description = "desc",
                duration = "01:02:03",
                date = LocalDate.of(2018, Month.JUNE, 10).atStartOfDay()
        )

        assertThat(boclipsVideoRepository.getVideoMetadata(setOf("reference-id-1"))).containsExactly(
                BoclipsVideo(
                        id = "1",
                        referenceId = "reference-id-1",
                        title = "great title",
                        contentProvider = "Bloomie",
                        contentProviderId = "b1",
                        duration = "01:02:03",
                        date = LocalDate.of(2018, Month.JUNE, 10).atStartOfDay(),
                        description = "desc"
                )
        )
    }

}
