package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.domain.model.subject.SubjectId

sealed class VideoFilter {
    data class ContentPartnerNameIs(val contentPartnerName: String) : VideoFilter()
    data class ContentPartnerIdIs(val contentPartnerId: ContentPartnerId) : VideoFilter()
    data class LegacyTypeIs(val type: LegacyVideoType) : VideoFilter()
    data class HasSubjectId(val subjectId: SubjectId) : VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
    object IsDownloadable : VideoFilter()
    object IsStreamable : VideoFilter()
}
