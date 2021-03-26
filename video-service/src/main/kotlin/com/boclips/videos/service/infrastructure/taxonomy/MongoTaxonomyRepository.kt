package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
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

    override fun create(taxonomy: Taxonomy): Taxonomy {
        val taxonomyDocument: TaxonomyDocument = TaxonomyDocumentConverter.toTaxonomyDocument(taxonomy)
        getTaxonomyCollection().insertOne(taxonomyDocument)
        return taxonomy
    }
    override fun findAll(): List<Taxonomy> {
        return getTaxonomyCollection().find().map { TaxonomyDocumentConverter.toTaxonomy(it) }.toList()
    }

    private fun getTaxonomyCollection(): MongoCollection<TaxonomyDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<TaxonomyDocument>(collectionName)
    }
}
