package com.boclips.search.service.testsupport

import com.boclips.search.service.infrastructure.collections.CollectionIndexReader
import com.boclips.search.service.infrastructure.collections.CollectionIndexWriter
import com.boclips.search.service.infrastructure.contract.CollectionSearchServiceFake
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class CollectionSearchProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = CollectionSearchServiceFake()

        val esClient = EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient()
        val readSearchService = CollectionIndexReader(esClient)
        val writeSearchService = CollectionIndexWriter.createTestInstance(esClient, 20)

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(readSearchService, writeSearchService)
        )
    }
}
