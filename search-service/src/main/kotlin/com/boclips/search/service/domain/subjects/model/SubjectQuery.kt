package com.boclips.search.service.domain.subjects.model

import com.boclips.search.service.domain.common.model.SearchQuery

class SubjectQuery(
    override val phrase: String = "",
) : SearchQuery<SubjectMetadata>(phrase)
