package com.boclips.search.service.common

import com.boclips.search.service.domain.common.model.PagingCursor

class InvalidCursorException(val cursor: PagingCursor) : RuntimeException()
