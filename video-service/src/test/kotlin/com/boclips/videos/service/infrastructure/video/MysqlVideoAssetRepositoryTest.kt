package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.infrastructure.video.subject.SubjectRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.stream.Stream

class MysqlVideoAssetRepositoryTest {

    @Test
    fun `streamAll ignores invalid assets`() {
        val subjectRepository = mock<SubjectRepository>()

        val videoEntityRepository = mock<VideoEntityRepository> {
            on { readAll() } doReturn Stream.of(
                    TestFactories.createVideoEntity(title = "corrupt item").apply { playback_id = null },
                    TestFactories.createVideoEntity(title = "this one is ok")
            )
        }

        val repository = MysqlVideoAssetRepository(subjectRepository, videoEntityRepository)
        var assets: List<VideoAsset> = emptyList()
        repository.streamAll { assets = it.toList() }

        assertThat(assets.map { it.title }).containsExactly("this one is ok")
    }

    @Test
    fun `streamAll closes the stream after the use`() {
        var closeCalled = false
        val subjectRepository = mock<SubjectRepository>()

        val videoEntityRepository = mock<VideoEntityRepository> {
            on { readAll() } doReturn Stream.of(
                    TestFactories.createVideoEntity()
            ).onClose {
                closeCalled = true
            }
        }

        val repository = MysqlVideoAssetRepository(subjectRepository, videoEntityRepository)
        repository.streamAll {  }

        assertThat(closeCalled)
                .overridingErrorMessage("""
                    Expected stream to be closed but it wasn't.
                    In kotlin we should apply `use` (https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/use.html),
                    in place of Java's `try-with-resources` (https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
                """)
                .isTrue()
    }
}