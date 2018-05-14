package com.boclips.api.presentation.resources

import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.core.Relation

data class ContentProvider(val name: String) : ResourceSupport()
