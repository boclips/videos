package com.boclips.videos.service.application.collection

import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.AbstractAssert
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
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
                    description = "for super-user, get all collections with no filter",
                    availableCollections = listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { it },
                    filter = TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }

                ),
                TestCase(
                    description = "for super-user, get all public collections with public = true",
                    availableCollections = listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it.first()) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }

                ),
                TestCase(
                    description = "for super-user, get all private collections with public = false",
                    availableCollections = listOf(
                        SaveCollectionRequest(public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it.last()) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = false),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
                ),
                TestCase(
                    description = "for super-user, get all collections which are bookmarked by self",
                    availableCollections = listOf(
                        SaveCollectionRequest(public = true, bookmarkedBy = "super-user", owner = "other-user"),
                        SaveCollectionRequest(public = true, bookmarkedBy = "another-user", owner = "different-user")
                    ),
                    chooseExpectedCollections = { listOf(it.first()) },
                    filter = TestFactories.CollectionsRequestFactory.sample(bookmarked = true),
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
                    description = "for specific owner, get all owned collections along with public collections",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { it.dropLast(1) },
                    filter = TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { UserFactory.sample(accessRulesSupplier = { access }) }
                ),
                TestCase(
                    description = "for specific owner, get all public collections with public = true",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[0], it[2]) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = true),
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
                    description = "for specific ids, get all specified collections",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[0], it[3]) },
                    filter = TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = { collectionIds ->
                        UserFactory.sample(accessRulesSupplier = {
                            AccessRulesFactory.specificIds(collectionIds[0], collectionIds[3])
                        })
                    }
                ),
                TestCase(
                    description = "for specific ids, get all specified collections public = true",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[0]) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = true),
                    buildUserWithAccessRules = { collectionIds ->
                        UserFactory.sample(accessRulesSupplier = {
                            AccessRulesFactory.specificIds(collectionIds[0], collectionIds[3])
                        })
                    }
                ),
                TestCase(
                    description = "for specific ids, get all specified collections with public = false",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[3]) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = false),
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
                    description = "for public only, gets just public collections",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[0], it[2]) },
                    filter = TestFactories.CollectionsRequestFactory.unfiltered(),
                    buildUserWithAccessRules = {
                        UserFactory.sample(accessRulesSupplier = { access })
                    }
                ),
                TestCase(
                    description = "for public only, get all public collections with public = true",
                    availableCollections = listOf(
                        SaveCollectionRequest(owner = "my-id", public = true),
                        SaveCollectionRequest(owner = "my-id", public = false),
                        SaveCollectionRequest(owner = "yet-another-user", public = true),
                        SaveCollectionRequest(owner = "another-user", public = false)
                    ),
                    chooseExpectedCollections = { listOf(it[0], it[2]) },
                    filter = TestFactories.CollectionsRequestFactory.sample(public = true),
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

    class CollectionsAssert(private val availableIds: List<CollectionId>, actual: ResultsPage<Collection, Nothing>) :
        AbstractAssert<CollectionsAssert, ResultsPage<Collection, Nothing>>(actual, CollectionsAssert::class.java) {

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
            fun assertThat(
                availableIds: List<CollectionId>,
                actual: ResultsPage<Collection, Nothing>
            ): CollectionsAssert {
                return CollectionsAssert(availableIds, actual)
            }
        }
    }
}
