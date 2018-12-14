package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.CreateVideoRequest;
import com.boclips.videos.service.client.VideoId;
import com.boclips.videos.service.client.VideoServiceClient;
import com.boclips.videos.service.client.exceptions.VideoNotFoundException;
import com.boclips.videos.service.client.spring.Video;
import lombok.val;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ApiClient implements VideoServiceClient {

    private final String baseUrl;

    private final RestTemplate restTemplate;

    public ApiClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public VideoId create(CreateVideoRequest request) {
        val uri = restTemplate.postForLocation(String.format("%s/v1/videos", baseUrl), request, String.class);
        return new VideoId(uri);
    }

    @Override
    public Boolean existsByContentPartnerInfo(String contentPartnerId, String contentPartnerVideoId) {
        try {
            restTemplate.headForHeaders(String.format("%s/v1/content-partners/%s/videos/%s", baseUrl, contentPartnerId, contentPartnerVideoId));
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == NOT_FOUND) {
                return false;
            } else {
                throw new UnsupportedOperationException(e);
            }
        }
    }

    @Override
    public void setSubjects(VideoId id, Set<String> subjects) {
        val body = new HashMap<String, Object>();
        body.put("subjects", subjects);
        try {
            restTemplate.postForObject(id.getUri(), body, String.class);
        } catch (HttpClientErrorException e) {
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    throw new VideoNotFoundException(id);
                default:
                    throw new UnsupportedOperationException(e);
            }
        }
    }

    @Override
    public Video get(VideoId id) {
        return restTemplate.getForObject(id.getUri(), VideoResource.class).toVideo();
    }
}
