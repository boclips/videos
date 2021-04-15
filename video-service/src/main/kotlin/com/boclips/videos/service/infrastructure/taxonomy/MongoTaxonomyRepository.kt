package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.converters.TaxonomyDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.getCollection


class MongoTaxonomyRepository(private val mongoClient: MongoClient) : TaxonomyRepository {

    companion object : KLogging() {
        const val collectionName = "taxonomy"
    }

    override fun create(taxonomyCategory: TaxonomyCategory): TaxonomyCategory {
        val taxonomyCategoryDocument: TaxonomyCategoryDocument = TaxonomyDocumentConverter.toTaxonomyDocument(taxonomyCategory)
        getTaxonomyCollection().insertOne(taxonomyCategoryDocument)
        return taxonomyCategory
    }
    override fun findAll(): List<TaxonomyCategory> {
        return getTaxonomyCollection().find().map { TaxonomyDocumentConverter.toTaxonomy(it) }.toList()
    }

    override fun findByCode(codeValue: String): TaxonomyCategory {
        TODO("Not yet implemented")
    }

    private fun getTaxonomyCollection(): MongoCollection<TaxonomyCategoryDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<TaxonomyCategoryDocument>(collectionName)
    }
}
