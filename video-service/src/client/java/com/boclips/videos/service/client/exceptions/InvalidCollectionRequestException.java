package com.boclips.videos.service.client.exceptions;

public class InvalidCollectionRequestException extends RuntimeException {
    public InvalidCollectionRequestException(Exception cause) {
        super(cause);
    }

    public InvalidCollectionRequestException(String message) {
        super(message);
    }
}
