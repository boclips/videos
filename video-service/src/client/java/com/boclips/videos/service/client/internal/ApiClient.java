package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import com.boclips.videos.service.client.internal.resources.*;
import lombok.val;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
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
    public Video get(VideoId id) {
        return restTemplate.getForObject(id.getUri(), VideoResource.class).toVideo();
    }

    @Override
    public VideoId rawIdToVideoId(String rawId) {
        return new VideoId(
                interpolateSingleResourceUri(getLinks().get_links().getVideo(), rawId)
        );
    }

    @Override
    public Collection get(CollectionId id) {
        return restTemplate.getForObject(id.getUri(), CollectionResource.class).toCollection();
    }

    @Override
    public CollectionId rawIdToCollectionId(String rawId) {
        return new CollectionId(
                interpolateSingleResourceUri(getLinks().get_links().getCollection(), rawId)
        );
    }

    private URI interpolateSingleResourceUri(Link singleResourceLink, String rawId) {
        val params = new HashMap<String, Object>();
        params.put("id", rawId);
        Link interpolatedLink = singleResourceLink.interpolate(params);
        return interpolatedLink.toUri();
    }

    @Override
    public List<Subject> getSubjects() {
        this.linkTemplate = getLinks();
        return restTemplate.getForObject(
                linkTemplate.get_links().getSubjects().toUri(), SubjectsResource.class)
                .toSubjects();
    }

    @Override
    public List<Collection> getMyCollections(PageSpec pageSpec) {
        this.linkTemplate = getLinks();
        Link collectionsLink = linkTemplate.get_links().getMyCollections();
        if (collectionsLink == null) {
            throw new UnsupportedOperationException("No 'my collections' link. Check user roles.");
        }
        return getCollections(uri(collectionsLink), pageSpec);
    }

    @Override
    public List<Collection> getCollectionsByOwner(String owner, PageSpec pageSpec) {
        this.linkTemplate = getLinks();
        Link collectionsLink = linkTemplate.get_links().getCollectionsByOwner();
        if (collectionsLink == null) {
            throw new UnsupportedOperationException("No 'collections by owner' link. Check user roles.");
        }
        return getCollections(uri(collectionsLink).queryParam("owner", owner), pageSpec);
    }

    private List<Collection> getCollections(UriComponentsBuilder uri, PageSpec pageSpec) {
        if (pageSpec.getPageSize() != null) {
            uri.replaceQueryParam("size", pageSpec.getPageSize());
        }

        return restTemplate.getForObject(uri.build().toUri(), CollectionsResource.class)
                .getCollections().stream()
                .map(CollectionResource::toCollection)
                .collect(Collectors.toList());
    }

    private UriComponentsBuilder uri(Link link) {
        return UriComponentsBuilder.fromUri(link.toUri());
    }

    private LinksResource getLinks() {
        if (linkTemplate == null) {
            linkTemplate = restTemplate.getForObject(baseUrl + "/v1", LinksResource.class);
        }

        return linkTemplate;
    }
}
