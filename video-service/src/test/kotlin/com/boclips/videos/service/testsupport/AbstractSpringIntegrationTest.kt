package com.boclips.videos.service.testsupport

import com.boclips.videos.service.infrastructure.video.VideoEntity
import com.boclips.videos.service.infrastructure.video.VideoRepository
import com.boclips.videos.service.testsupport.fakes.FakeSearchService
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-kaltura", "fake-search", "fake-requestid")
@Transactional
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var repos: Set<MongoRepository<*, *>>

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var fakeSearchService: FakeSearchService

    @Before
    fun cleanDatabases() {
        repos.forEach { it.deleteAll() }
        videoRepository.deleteAll()
        fakeSearchService.reset()
    }

    fun saveVideo(videoId: Long,
                  title: String = "Some title!",
                  description: String = "Some description!",
                  date: String = "2018-01-01",
                  duration: String = "00:10:00",
                  contentProvider: String = "AP",
                  referenceId: String = "ref-id-1") {
        videoRepository.save(VideoEntity(
                id = videoId,
                reference_id = referenceId,
                title = title,
                duration = duration,
                description = description,
                date = date,
                source = contentProvider))
    }
}
