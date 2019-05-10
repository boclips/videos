package com.boclips.videos.service.application.video.exceptions

class InvalidDateException(date: String) :
    VideoServiceException("$date is not a valid YYYY-MM-DD Date. Example is 2001-04-21.") {

}
