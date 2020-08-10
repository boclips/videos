package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchSubjectsContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by manually-tagged subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjectsSetManually = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjectsSetManually = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjectsSetManually = null
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    userQuery = UserQuery(subjectsSetManually = true),
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsAll(listOf("0"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "2"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by non-manually-tagged subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjectsSetManually = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjectsSetManually = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjectsSetManually = null
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    userQuery = UserQuery(subjectsSetManually = false),
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsAll(listOf("1"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0", "2"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by subject on videos with multiple subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(
                        TestFactories.createSubjectMetadata(id = "subject-one"),
                        TestFactories.createSubjectMetadata(id = "subject-two")
                    )
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(TestFactories.createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(
                        TestFactories.createSubjectMetadata("subject-two"),
                        TestFactories.createSubjectMetadata("subject-three")
                    )
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    userQuery = UserQuery(subjectIds = setOf("subject-three")),
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsAll(listOf("1", "2"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter with multiple subjects in a query`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(TestFactories.createSubjectMetadata("subject-one"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(TestFactories.createSubjectMetadata("subject-two"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(
                        TestFactories.createSubjectMetadata("subject-two"),
                        TestFactories.createSubjectMetadata("subject-three")
                    )
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf(
                        TestFactories.createSubjectMetadata("subject-four"),
                        TestFactories.createSubjectMetadata("subject-three")
                    )
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    userQuery = UserQuery(subjectIds = setOf("subject-one", "subject-two")),
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsAll(listOf("0", "1", "2"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("3"))
    }
}
