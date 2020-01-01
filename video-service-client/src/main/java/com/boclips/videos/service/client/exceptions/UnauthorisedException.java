package com.boclips.videos.service.client.exceptions;

public class UnauthorisedException extends RuntimeException {
    public UnauthorisedException(String message) {
        super(message);
    }
}
