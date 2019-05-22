package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import com.boclips.videos.service.client.exceptions.VideoNotFoundException;
import com.boclips.videos.service.client.internal.resources.*;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

public class ApiClient implements VideoServiceClient {

    private final String baseUrl;

    private final RestTemplate restTemplate;

    private LinksResource linkTemplate;

    public ApiClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public VideoId create(CreateVideoRequest request) {
        try {
            val uri = restTemplate.postForLocation(baseUrl + "/v1/videos", request);
            return new VideoId(uri);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(CONFLICT)) {
                throw new VideoExistsException();
            } else if (e.getStatusCode().equals(BAD_REQUEST)) {
                throw new IllegalVideoRequestException();
            }
            throw e;
        }
    }

    @Override
    public Boolean existsByContentPartnerInfo(String contentPartnerId, String contentPartnerVideoId) {
        try {
            restTemplate.headForHeaders(baseUrl + "/v1/content-partners/{contentPartnerId}/videos/{contentPartnerVideoId}", contentPartnerId, contentPartnerVideoId);
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

    @Override
    @SneakyThrows
    public VideoId rawIdToVideoId(String rawId) {
        this.linkTemplate = getLinks();
        val videoLinkTemplate = linkTemplate.get_links().getVideo();
        val params = new HashMap<String, Object>();
        params.put("id", rawId);
        Link videoLink = videoLinkTemplate.interpolate(params);
        return new VideoId(videoLink.toUri());
    }

    @Override
    public List<Subject> getSubjects() {
        this.linkTemplate = getLinks();
        return restTemplate.getForObject(
                linkTemplate.get_links().getSubjects().toUri(), SubjectsResource.class)
                .toSubjects();
    }

    @Override
    public List<Collection> getMyCollections() {
        this.linkTemplate = getLinks();
        Link collectionsLink = linkTemplate.get_links().getMyCollections();
        if(collectionsLink == null) {
            throw new UnsupportedOperationException("No 'my collections' link. Check user roles.");
        }
        return getCollections(collectionsLink.toUri());
    }

    @Override
    public List<Collection> getCollectionsByOwner(String owner) {
        this.linkTemplate = getLinks();
        Link collectionsLink = linkTemplate.get_links().getCollectionsByOwner();
        if(collectionsLink == null) {
            throw new UnsupportedOperationException("No 'collections by owner' link. Check user roles.");
        }
        return getCollections(URI.create(collectionsLink.getHref() + "&owner=" + owner));
    }

    private List<Collection> getCollections(URI uri) {
        return restTemplate.getForObject(uri, CollectionsResource.class)
                .getCollections().stream()
                .map(CollectionResource::toCollection)
                .collect(Collectors.toList());
    }

    private LinksResource getLinks() {
        if (linkTemplate == null) {
            linkTemplate = restTemplate.getForObject(baseUrl + "/v1", LinksResource.class);
        }

        return linkTemplate;
    }
}
