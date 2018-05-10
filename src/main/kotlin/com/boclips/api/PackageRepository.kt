package com.boclips.api

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.transaction.annotation.Transactional

interface PackageRepository : ReactiveMongoRepository<Package, Long>