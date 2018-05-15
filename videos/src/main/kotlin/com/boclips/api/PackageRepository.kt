package com.boclips.api

import com.boclips.api.infrastructure.PackageEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PackageRepository : ReactiveMongoRepository<PackageEntity, String>