package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.*;
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
import static org.springframework.http.HttpStatus.*;

public class ApiClient implements VideoServiceClient {

    private static final String DETAILS_PROJECTION = "details";

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
            restTemplate.postForEntity(baseUrl + "/v1/content-partners/{contentPartnerId}/videos/search", contentPartnerVideoId, String.class, contentPartnerId);
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
    public ContentPartner findContentPartner(ContentPartnerId id) {
        val params = new HashMap<String, Object>();
        params.put("id", id.getValue());
        URI uri = getLinks().get_links().getContentPartner().interpolate(params).toUri();

        return Objects.requireNonNull(
                restTemplate.getForObject(uri, ContentPartnerResource.class)
        ).toContentPartner();
    }

    @Override
    public List<ContentPartner> findOfficialContentPartner(String name) {
        val params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("official", true);

        return fetchContentPartners(params);
    }

    @Override
    public List<ContentPartner> findContentPartnerByYoutubeChannelId(String youtubeChannelId) {
        val params = new HashMap<String, Object>();
        params.put("accreditedToYtChannelId", youtubeChannelId);

        return fetchContentPartners(params);
    }

    @Override
    public List<ContentPartner> getContentPartners() {
        return fetchContentPartners(new HashMap<>());
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
                new URI(id.getUri().toString() + "?projection=" + DETAILS_PROJECTION),
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
        this.linkTemplate = getLinks();
        Link searchCollections = linkTemplate.get_links().getSearchCollections();
        if (searchCollections == null) {
            throw new UnsupportedOperationException("No 'searchCollections' link. Check user roles.");
        }

        return getCollections(
                uri(searchCollections.interpolate(singletonMap("projection", DETAILS_PROJECTION))),
                pageSpec
        );
    }

    @Override
    public List<Collection> getMyCollections(PageSpec pageSpec) {
        return getCollections(uri(myCollectionsLink()), pageSpec);
    }

    @Override
    public List<Collection> getMyCollectionsDetailed(PageSpec pageSpec) {
        return getCollections(
                uri(myCollectionsLink()).replaceQueryParam("projection", DETAILS_PROJECTION),
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

    @Override
    public CollectionId createCollection(CreateCollectionRequest request) {
        val createCollectionLink = this.getLinks().get_links().getCreateCollection();

        if (createCollectionLink == null) {
            throw new UnauthorisedException("Create collection link is missing");
        }

        try {
            val uri = restTemplate.postForLocation(createCollectionLink.toUri(), request);
            return new CollectionId(uri);
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new InvalidCollectionRequestException(exception);
        }
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

    private List<ContentPartner> fetchContentPartners(HashMap<String, Object> params) {
        URI uri = getLinks().get_links().getContentPartners().interpolate(params).toUri();

        return Objects.requireNonNull(
                restTemplate.getForObject(uri, ContentPartnersResource.class)
        ).toContentPartners();
    }
}