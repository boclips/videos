package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import java.net.URL
import java.time.Period
import java.util.Currency

sealed class ChannelUpdateCommand(val channelId: ChannelId) {

    class ReplaceName(channelId: ChannelId, val name: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceAgeRanges(channelId: ChannelId, val ageRangeBuckets: AgeRangeBuckets) :
        ChannelUpdateCommand(channelId)

    class ReplaceDistributionMethods(
        channelId: ChannelId, val distributionMethods: Set<DistributionMethod>
    ) : ChannelUpdateCommand(channelId)

    class ReplaceLegalRestrictions(channelId: ChannelId, val legalRestriction: LegalRestriction) :
        ChannelUpdateCommand(channelId)

    class ReplaceCurrency(channelId: ChannelId, val currency: Currency) :
        ChannelUpdateCommand(channelId)

    class ReplaceContentTypes(channelId: ChannelId, val contentType: List<String>) :
        ChannelUpdateCommand(channelId)

    class ReplaceContentCategories(channelId: ChannelId, val contentCategories: List<ContentCategory>) :
        ChannelUpdateCommand(channelId)

    class ReplaceLanguage(channelId: ChannelId, val language: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceDescription(channelId: ChannelId, val description: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceAwards(channelId: ChannelId, val awards: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceHubspotId(channelId: ChannelId, val hubspotId: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceNotes(channelId: ChannelId, val notes: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceMarketingStatus(channelId: ChannelId, val status: ChannelStatus) :
        ChannelUpdateCommand(channelId)

    class ReplaceMarketingLogos(channelId: ChannelId, val logos: List<URL>) :
        ChannelUpdateCommand(channelId)

    class ReplaceMarketingShowreel(channelId: ChannelId, val showreel: URL?) :
        ChannelUpdateCommand(channelId)

    class ReplaceMarketingSampleVideos(channelId: ChannelId, val sampleVideos: List<URL>) :
        ChannelUpdateCommand(channelId)

    class ReplaceOneLineDescription(channelId: ChannelId, val oneLineDescription: String) :
        ChannelUpdateCommand(channelId)

    class ReplaceBestForTags(channelId: ChannelId, val bestForTags: List<String>) :
        ChannelUpdateCommand(channelId)

    class ReplaceSubjects(channelId: ChannelId, val subjects: List<String>) :
        ChannelUpdateCommand(channelId)

    class ReplaceIngestDetails(channelId: ChannelId, val ingest: IngestDetails) :
        ChannelUpdateCommand(channelId)

    class ReplaceDeliveryFrequency(channelId: ChannelId, val deliveryFrequency: Period) :
        ChannelUpdateCommand(channelId)

    class ReplaceContract(channelId: ChannelId, val contract: Contract) :
        ChannelUpdateCommand(channelId)

    class ReplaceCategories(channelId: ChannelId, val categories: Set<CategoryWithAncestors>) :
        ChannelUpdateCommand(channelId)
}
