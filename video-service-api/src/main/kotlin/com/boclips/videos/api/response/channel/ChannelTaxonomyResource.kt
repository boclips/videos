import com.boclips.videos.api.response.channel.TaxonomyCategoryResource

data class ChannelTaxonomyResource(
    val categories: List<TaxonomyCategoryResource>? = null,
    val requiresVideoLevelTagging: Boolean? = null,
)
