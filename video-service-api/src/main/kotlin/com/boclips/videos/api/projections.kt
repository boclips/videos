package com.boclips.videos.api

interface ResourceProjection
interface PublicApiProjection : ResourceProjection
interface BoclipsInternalProjection : PublicApiProjection
interface PricingProjection : PublicApiProjection
