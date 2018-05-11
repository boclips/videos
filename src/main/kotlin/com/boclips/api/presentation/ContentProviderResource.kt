package com.boclips.api.presentation

import org.springframework.hateoas.ResourceSupport

data class ContentProviderResource(val name: String) : ResourceSupport()
