package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.asset.AssetId

interface CollectionUpdateCommand

class AddVideoToCollectionCommand(val videoId: AssetId) :
    CollectionUpdateCommand

class RemoveVideoFromCollectionCommand(val videoId: AssetId) :
    CollectionUpdateCommand
