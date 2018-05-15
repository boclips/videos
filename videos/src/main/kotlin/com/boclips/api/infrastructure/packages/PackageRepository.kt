package com.boclips.api.infrastructure.packages

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PackageRepository : ReactiveMongoRepository<PackageEntity, String>