package com.boclips.cleanser.infrastructure.boclips

import com.boclips.testsupport.AbstractSpringIntegrationTest
import com.boclips.testsupport.insert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class BoclipsVideoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var boclipsVideoRepository: BoclipsVideoRepository

    @Test
    fun getAllIds() {
        jdbcTemplate.update(insert(id = "1", title = "great title", contentProvider = "Bloomie"))
        jdbcTemplate.update(insert(id = "2"))

        assertThat(boclipsVideoRepository.getAllVideos().first().id).isEqualTo("1")
        assertThat(boclipsVideoRepository.getAllVideos().first().title).isEqualTo("great title")
        assertThat(boclipsVideoRepository.getAllVideos().first().contentProvider).isEqualTo("Bloomie")
    }

    @Test
    fun getAllIds_prefersReferenceIdOverId() {
        jdbcTemplate.update(insert(id = "1", referenceId = "a reference id"))
        jdbcTemplate.update(insert(id = "2"))

        assertThat(boclipsVideoRepository.getAllVideos().map { it.id }).containsExactly("a reference id", "2")
    }

    @Test
    fun countAllVideos() {
        assertThat(boclipsVideoRepository.countAllVideos()).isEqualTo(0)

        jdbcTemplate.update(insert(id = "1"))
        jdbcTemplate.update(insert(id = "2"))

        assertThat(boclipsVideoRepository.countAllVideos()).isEqualTo(2)
    }

}
