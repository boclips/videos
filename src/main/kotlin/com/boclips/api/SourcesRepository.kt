package com.boclips.api

import org.springframework.data.mongodb.repository.MongoRepository


interface SourcesRepository : MongoRepository<Source, String> {
    fun findByName(name: String): Source?
}