package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.ContentPartnerExistsException;
import com.boclips.videos.service.client.exceptions.IllegalContentPartnerRequestException;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import com.boclips.videos.service.client.internal.resources.*;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
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
    public VideoId createVideo(CreateVideoRequest request) {
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
    public ContentPartnerId createContentPartner(CreateContentPartnerRequest request) {
        try {
            val uri = restTemplate.postForLocation(baseUrl + "/v1/content-partners", request);
            val contentPartnerId = UriIdExtractor.extractId(uri, UriIdExtractor.CONTENT_PARTNER_ID_URI_PATTERN);
            return ContentPartnerId
                    .builder()
                    .value(contentPartnerId)
                    .build();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(CONFLICT)) {
                throw new ContentPartnerExistsException();
            } else if (e.getStatusCode().equals(BAD_REQUEST)) {
                throw new IllegalContentPartnerRequestException();
            }
            throw e;
        }
    }

    @Override
    public List<ContentPartner> findOfficialContentPartner(String name) {
        val params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("official", true);

        return getContentPartners(params);
    }

    @Override
    public List<ContentPartner> findContentPartnerByYoutubeChannelId(String youtubeChannelId) {
        val params = new HashMap<String, Object>();
        params.put("accreditedToYtChannelId", youtubeChannelId);

        return getContentPartners(params);
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
    @SneakyThrows
    public Collection getDetailed(CollectionId id) {
        return restTemplate.getForObject(
                new URI(id.getUri().toString() + "?projection=details"),
                CollectionResource.class
        ).toCollection();
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
    public List<Collection> getCollectionsDetailed(PageSpec pageSpec) {
        // TODO Remove this hack and implement a proper way of retrieving viewable collections
        this.linkTemplate = getLinks();
        Link collectionLink = linkTemplate.get_links().getCollection();
        if (collectionLink == null) {
            throw new UnsupportedOperationException("No 'collection' link. Check user roles.");
        }

        List<Collection> viewerCollections = getCollections(
                uri(collectionLink.interpolate(singletonMap("id", "dont-do-this-at-home"))),
                pageSpec
        );

        List<Collection> myCollections = getMyCollectionsDetailed(pageSpec);

        return concat(viewerCollections.stream(), myCollections.stream()).collect(toList());
    }

    @Override
    public List<Collection> getMyCollections(PageSpec pageSpec) {
        return getCollections(uri(myCollectionsLink()), pageSpec);
    }

    @Override
    public List<Collection> getMyCollectionsDetailed(PageSpec pageSpec) {
        return getCollections(
                uri(myCollectionsLink()).replaceQueryParam("projection", "details"),
                pageSpec
        );
    }

    private Link myCollectionsLink() {
        this.linkTemplate = getLinks();
        Link myCollectionsLink = linkTemplate.get_links().getMyCollections();
        if (myCollectionsLink == null) {
            throw new UnsupportedOperationException("No 'my collections' link. Check user roles.");
        }
        return myCollectionsLink;
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
                .collect(toList());
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

    private List<ContentPartner> getContentPartners(HashMap<String, Object> params) {
        String url = getLinks().get_links().getContentPartners().interpolate(params).getHref();

        return Objects.requireNonNull(restTemplate.getForObject(
                url,
                ContentPartnersResource.class
        )).toContentPartners();
    }
}
