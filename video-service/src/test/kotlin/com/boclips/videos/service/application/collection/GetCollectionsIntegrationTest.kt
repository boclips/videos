package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
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
        val filter: CollectionsController.CollectionsRequest,
        val buildUserWithAccessRules: (List<CollectionId>) -> User
    )

    companion object {
        private val superuserTests = AccessRulesFactory.superuser().let { access ->
            listOf(
                TestCase(
                    "for super-user, get all collections with no filter",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { it },
                    TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }

                ),
                TestCase(
                    "for super-user, get all public collections with public = true",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it.first()) },
                    TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }

                ),
                TestCase(
                    "for super-user, get all private collections with public = false",
                    listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    { listOf(it.last()) },
                    TestFactories.CollectionsRequestFactory.sample(public = false),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
                ),
                TestCase(
                    "for super-user, get all collections which are bookmarked by self",
                    listOf(
                        SaveCollectionRequest(public = true, bookmarkedBy = "super-user", owner = "other-user"),
                        SaveCollectionRequest(public = true, bookmarkedBy = "another-user", owner = "different-user")
                    ),
                    { listOf(it.first()) },
                    TestFactories.CollectionsRequestFactory.sample(bookmarked = true),
                    buildUserWithAccessRules = {
                        UserFactory.sample(
                            id = "super-user",
                            accessRulesSupplier = { access })
                    }
                )
            )
        }

        private val specificOwnerTests = AccessRulesFactory.asOwner("my-id").let { access ->
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
                    TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
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
                    TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
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
                    TestFactories.CollectionsRequestFactory.sample(public = false),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
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
                    TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { collectionIds ->
                        UserFactory.sample(accessRulesSupplier = {
                            AccessRulesFactory.specificIds(collectionIds[0], collectionIds[3])
                        })
                    }
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
                    TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = { collectionIds ->
                        UserFactory.sample(accessRulesSupplier = {
                            AccessRulesFactory.specificIds(collectionIds[0], collectionIds[3])
                        })
                    }
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
                    TestFactories.CollectionsRequestFactory.sample(public = false),
                    buildUserWithAccessRules = { collectionIds ->
                        UserFactory.sample(accessRulesSupplier = {
                            AccessRulesFactory.specificIds(collectionIds[0], collectionIds[3])
                        })
                    }
                )
            )

        private val publicOnlyTests = AccessRulesFactory.publicOnly().let { access ->
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
                    TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = {
                        UserFactory.sample(accessRulesSupplier = { access })
                    }
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
                    TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = {
                        UserFactory.sample(accessRulesSupplier = { access })
                    }
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
            testCase.buildUserWithAccessRules(availableIds)
        )

        CollectionsAssert.assertThat(availableIds, collectionPage)
            .containsCollectionsWithIds(testCase.chooseExpectedCollections(availableIds))
    }

    @Test
    fun `trying to get private collections when public only access`() {
        assertThrows<OperationForbiddenException> {
            getCollections.getUnassembledCollections(
                collectionsRequest = TestFactories.CollectionsRequestFactory.sample(public = false),
                user = UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.publicOnly() })
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
