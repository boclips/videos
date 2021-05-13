import com.boclips.videos.api.response.channel.TaxonomyCategoryResource

data class TaxonomyResource(
    val categories: List<TaxonomyCategoryResource>? = null,
    val requiresVideoLevelTagging: Boolean? = null,
)

data class TaxonomyResourceWrapper(
    val channel: TaxonomyResource,
    val manual: TaxonomyResource
)
