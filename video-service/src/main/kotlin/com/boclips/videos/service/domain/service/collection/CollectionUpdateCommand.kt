package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.asset.AssetId

interface CollectionUpdateCommand

class AddVideoToCollection(val videoId: AssetId) :
    CollectionUpdateCommand
class RemoveVideoFromCollection(val videoId: AssetId) :
    CollectionUpdateCommand
