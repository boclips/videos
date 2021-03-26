package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyCategory (
    val codeValue: String,
    val description: String,
    val parentCode: String? = null
)
