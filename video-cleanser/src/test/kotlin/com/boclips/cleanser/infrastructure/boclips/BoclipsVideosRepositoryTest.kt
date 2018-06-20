package com.boclips.cleanser.infrastructure.boclips

import com.boclips.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class BoclipsVideosRepositoryTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var boclipsVideosRepository: BoclipsVideosRepository

    @Test
    fun getAllIds() {
        addRow(id = "1", title = "great title", contentProvider = "Bloomie")
        addRow(id = "2")

        assertThat(boclipsVideosRepository.getAllVideos().first().id).isEqualTo("1")
        assertThat(boclipsVideosRepository.getAllVideos().first().title).isEqualTo("great title")
        assertThat(boclipsVideosRepository.getAllVideos().first().contentProvider).isEqualTo("Bloomie")
    }

    @Test
    fun getAllIds_prefersReferenceIdOverId() {
        addRow(id = "1", referenceId = "'a reference id'")
        addRow(id = "2")

        assertThat(boclipsVideosRepository.getAllVideos().map { it.id }).containsExactly("a reference id", "2")
    }

    @Test
    fun countAllVideos() {
        assertThat(boclipsVideosRepository.countAllVideos()).isEqualTo(0)

        addRow(id = "1")
        addRow(id = "2")

        assertThat(boclipsVideosRepository.countAllVideos()).isEqualTo(2)
    }

    private fun addRow(id: String? = "NULL", referenceId: String? = "NULL", title: String? = "some title", contentProvider: String? = "some cp") {
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id, title, source) " +
                "VALUES('$id', $referenceId, '$title', '$contentProvider')")
    }
}
