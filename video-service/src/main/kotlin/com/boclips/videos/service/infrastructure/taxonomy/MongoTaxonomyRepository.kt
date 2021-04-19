package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.converters.TaxonomyDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoTaxonomyRepository(private val mongoClient: MongoClient) : TaxonomyRepository {

    companion object : KLogging() {
        const val collectionName = "taxonomy"
    }

    override fun create(taxonomyCategory: Category): Category {
        val taxonomyNodeDocument: TaxonomyNodeDocument = TaxonomyDocumentConverter.toDocument(taxonomyCategory)
        getTaxonomyCollection().insertOne(taxonomyNodeDocument)
        return taxonomyCategory
    }

    override fun findAll(): List<Category> {
        return TaxonomyDocumentConverter.toCategories(getTaxonomyCollection().find().toList())
    }

    override fun findByCode(code: CategoryCode): Category? {
        val taxonomy = getTaxonomyCollection().findOne(TaxonomyCategoryDocument::codeValue eq code.value)
        return taxonomy?.let { TaxonomyDocumentConverter.toTaxonomy(it) }
    }

    private fun getTaxonomyCollection(): MongoCollection<TaxonomyNodeDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<TaxonomyNodeDocument>(collectionName)
    }
}
