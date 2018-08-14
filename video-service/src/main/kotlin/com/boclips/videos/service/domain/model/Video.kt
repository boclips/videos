package com.boclips.videos.service.domain.model

data class Video(
        public val id: String? = null,
        public val source: String? = null,
        public val unique_id: String? = null,
        public val namespace: String? = null,
        public val title: String? = null,
        public val description: String? = null,
        public val date: String? = null,
        public val duration: String? = null,
        public val keywords: String? = null,
        public val price_category: String? = null,
        public val sounds: String? = null,
        public val color: String? = null,
        public val location: String? = null,
        public val country: String? = null,
        public val state: String? = null,
        public val city: String? = null,
        public val region: String? = null,
        public val alternative_id: String? = null,
        public val alt_source: String? = null,
        public val restrictions: String? = null,
        public val type_id: String? = null,
        public val reference_id: String? = null
)