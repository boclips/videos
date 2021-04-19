package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoCategoryRepository(private val mongoClient: MongoClient) : CategoryRepository {

    companion object : KLogging() {
        const val collectionName = "taxonomy"
    }

    override fun create(category: Category): Category {
        val categoryDocument: CategoryDocument = CategoryDocumentConverter.toDocument(category)
        getTaxonomyCollection().insertOne(categoryDocument)
        return category
    }

    override fun findAll(): List<Category> {
        return getTaxonomyCollection().find().toList().map { CategoryDocumentConverter.toCategory(it) }
    }

    override fun findByCode(code: CategoryCode): Category? {
        val taxonomy = getTaxonomyCollection().findOne(CategoryDocument::codeValue eq code.value)
        return taxonomy?.let { CategoryDocumentConverter.toCategory(it) }
    }

    private fun getTaxonomyCollection(): MongoCollection<CategoryDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<CategoryDocument>(collectionName)
    }
}
