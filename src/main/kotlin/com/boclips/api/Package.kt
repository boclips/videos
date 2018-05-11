package com.boclips.api

import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.Id

@Document(collection = "packages")
data class Package(
        @Id
        val id: String,
        val name: String
)
