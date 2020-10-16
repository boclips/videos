package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class SubjectsIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    batchSize: Int
) :
    AbstractIndexWriter<SubjectMetadata>(
        indexConfiguration = SubjectIndexConfiguration(),
        indexParameters = indexParameters,
        client = client,
        esIndex = SubjectsIndex,
        batchSize = batchSize,
        ngram = true
    ) {
    companion object {
        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            batchSize: Int
        ): SubjectsIndexWriter {
            return SubjectsIndexWriter(client, indexParameters, batchSize)
        }

        fun createTestInstance(
            client: RestHighLevelClient,
            batchSize: Int
        ): SubjectsIndexWriter {
            return SubjectsIndexWriter(client, IndexParameters(numberOfShards = 1), batchSize)
        }
    }

    override fun serializeToIndexDocument(entry: SubjectMetadata) =
        SubjectsDocumentConverter().convertToDocument(entry)

    override fun getIdentifier(entry: SubjectMetadata) = entry.id
}
