package com.boclips.videos.service.application.collection

import com.boclips.security.utils.User
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.AccessRule
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRuleFactory
import com.boclips.videos.service.testsupport.CollectionsRequestFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.AbstractAssert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Stream

class GetCollectionsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getCollections: GetCollections

    data class TestCase(
        val description: String,
        val availableCollections: List<SaveCollectionRequest>,
        val chooseExpectedCollections: (List<CollectionId>) -> List<CollectionId>,
        val buildAccessRule: (List<CollectionId>) -> AccessRule,
        val filter: CollectionsController.CollectionsRequest,
        val user: User? = null
    )

    companion object {
        private val superuserTests = AccessRuleFactory.superuser().let { access ->
            listOf(
                TestCase(
                    "for super-user, get all collections with no filter",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { it },
                    { access },
                    CollectionsRequestFactory.unfiltered()

                ),
                TestCase(
                    "for super-user, get all public collections with public = true",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it.first()) },
                    { access },
                    CollectionsRequestFactory.sample(public = true)

                ),
                TestCase(
                    "for super-user, get all private collections with public = false",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it.last()) },
                    { access },
                    CollectionsRequestFactory.sample(public = false)
                ),
                TestCase(
                    "for super-user, get all collections which are bookmarked by self",
                    listOf(
                        SaveCollectionRequest(public = true, bookmarkedBy = "super-user", owner = "other-user"),
                        SaveCollectionRequest(public = true, bookmarkedBy = "another-user", owner = "different-user")
                    ),
                    { listOf(it.first()) },
                    { access },
                    CollectionsRequestFactory.sample(bookmarked = true),
                    user = UserFactory.sample(id = "super-user")
                )
            )
        }

        private val specificOwnerTests = AccessRuleFactory.asOwner("my-id").let { access ->
            listOf(
                TestCase(
                    "for specific owner, get all owned collections along with public collections",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { it.dropLast(1) },
                    { access },
                    CollectionsRequestFactory.unfiltered()
                ),
                TestCase(
                    "for specific owner, get all public collections with public = true",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[0], it[2]) },
                    { access },
                    CollectionsRequestFactory.sample(public = true)
                ),
                TestCase(
                    "for specific owner, get all owned private collections with public = false",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[1]) },
                    { access },
                    CollectionsRequestFactory.sample(public = false)
                )
            )
        }

        private val specificIdsTests =
            listOf(
                TestCase(
                    "for specific ids, get all specified collections",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[0], it[3]) },
                    { AccessRuleFactory.specificIds(it[0], it[3]) },
                    CollectionsRequestFactory.unfiltered()
                ),
                TestCase(
                    "for specific ids, get all specified collections public = true",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[0]) },
                    { AccessRuleFactory.specificIds(it[0], it[3]) },
                    CollectionsRequestFactory.sample(public = true)
                ),
                TestCase(
                    "for specific ids, get all specified collections with public = false",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[3]) },
                    { AccessRuleFactory.specificIds(it[0], it[3]) },
                    CollectionsRequestFactory.sample(public = false)
                )
            )

        private val publicOnlyTests = AccessRuleFactory.publicOnly().let { access ->
            listOf(
                TestCase(
                    "for public only, gets just public collections",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[0], it[2]) },
                    { access },
                    CollectionsRequestFactory.unfiltered()
                ),
                TestCase(
                    "for public only, get all public collections with public = true",
                    listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it[0], it[2]) },
                    { access },
                    CollectionsRequestFactory.sample(public = true)
                )
            )
        }

        val testCases = listOf(
            superuserTests,
            specificOwnerTests,
            specificIdsTests,
            publicOnlyTests
        ).flatten().stream()
    }

    class TestCaseProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<Arguments>? {
            return testCases.map { Arguments.of(it, it.description) }
        }
    }

    @ParameterizedTest(name = "{1}")
    @ArgumentsSource(TestCaseProvider::class)
    fun `get collections`(testCase: TestCase, description: String) {
        val availableIds = testCase.availableCollections.map { saveCollection(it) }

        val collectionPage = getCollections.getUnassembledCollections(
            testCase.filter,
            testCase.buildAccessRule(availableIds),
            testCase.user
        )

        CollectionsAssert.assertThat(availableIds, collectionPage)
            .containsCollectionsWithIds(testCase.chooseExpectedCollections(availableIds))
    }

    @Test
    fun `trying to get private collections when public only access`() {
        assertThrows<OperationForbiddenException> {
            getCollections.getUnassembledCollections(
                CollectionsRequestFactory.sample(public = false),
                AccessRuleFactory.publicOnly()
            )
        }
    }

    class CollectionsAssert(private val availableIds: List<CollectionId>, actual: Page<Collection>) :
        AbstractAssert<CollectionsAssert, Page<Collection>>(actual, CollectionsAssert::class.java) {

        fun containsCollectionsWithIds(expectedIds: Iterable<CollectionId>): CollectionsAssert {
            if (!(actual.elements.map { it.id }.containsAll(expectedIds.toList()))
                || actual.elements.toList().size != expectedIds.toList().size
            ) {
                failWithMessage(
                    """Expecting to keep collections with indices:
                    |${expectedIds.map { availableIds.indexOf(it) }}
                    |However, we received collections with these indices instead: 
                    |${actual.elements.map { availableIds.indexOf(it.id) }}
                    |Please note, the index refers to the collection's position in the list passed to the test."""
                        .trimMargin()
                )
            }
            return this
        }

        companion object {
            fun assertThat(availableIds: List<CollectionId>, actual: Page<Collection>): CollectionsAssert {
                return CollectionsAssert(availableIds, actual)
            }
        }
    }
}
