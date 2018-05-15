package com.boclips.api

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "metadata_orig")
data class Video(
        @Id
        val id: Long,
        val source: String
)
