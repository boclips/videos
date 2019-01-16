package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.infrastructure.video.subject.SubjectRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MysqlVideoAssetRepositoryTest {

    @Test
    fun `streamAll ignores invalid assets`() {
        val subjectRepository = mock<SubjectRepository>()
        val videoEntityRepository = mock<VideoEntityRepository>()

        val videoSequenceFactory = mock<VideoSequenceReader> {
            on { readOnly(any()) } doAnswer { invocation ->
                val consumer = invocation.arguments.first() as VideoEntitySequenceConsumer
                consumer(
                        sequenceOf(
                                TestFactories.createVideoEntity(title = "corrupt item").apply { playback_id = null },
                                TestFactories.createVideoEntity(title = "this one is ok")
                        )
                )
                null
            }
        }

        val repository = MysqlVideoAssetRepository(subjectRepository, videoEntityRepository, videoSequenceFactory)
        var assets: List<VideoAsset> = emptyList()
        repository.streamAll { assets = it.toList() }

        assertThat(assets.map { it.title }).containsExactly("this one is ok")
    }
}