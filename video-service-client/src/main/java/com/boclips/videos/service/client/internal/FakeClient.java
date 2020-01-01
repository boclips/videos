package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.Collection;
import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.InvalidCollectionRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import com.boclips.videos.service.client.internal.resources.Link;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class FakeClient implements VideoServiceClient {
    private List<CreateVideoRequest> createRequests = new ArrayList<>();
    private Map<VideoId, Video> videos = new HashMap<>();
    private Set<String> illegalPlaybackIds = new HashSet<>();
    private Map<VideoId, Playback> playbacks = new HashMap<>();
    private Map<SubjectId, Subject> subjects = new HashMap<>();
    private Map<String, List<Collection>> collectionsByUser = new HashMap<>();
    private Map<String, List<Collection>> detailedCollectionsByUser = new HashMap<>();
    private List<ContentPartner> contentPartners = new ArrayList<>();
    private Boolean useInternalProjection = false;

    @Override
    public VideoId createVideo(CreateVideoRequest request) {
        if (existsByContentPartnerInfo(request.getProviderId(), request.getProviderVideoId())) {
            throw new VideoExistsException();
        }

        if (illegalPlaybackIds.contains(request.getPlaybackId())) {
            throw new IllegalVideoRequestException();
        }

        val videoId = rawIdToVideoId(nextId());

        Playback playback = Playback.builder()
                .playbackId(request.getPlaybackId())
                .duration(Duration.ofMinutes(7))
                .referenceId("ref-" + request.getPlaybackId())
                .thumbnailUrl("https://thumbnailz.org/img/" + nextId())
                .links(new PlaybackLinks(
                        new Link("/events/playback")
                ))
                .build();

        Set<Subject> videoSubjects = request.getSubjects().stream()
                .map(subjectId -> {
                    return subjects.get(SubjectId.builder().value(subjectId).build());
                })
                .collect(toSet());

        final val createdBy = contentPartners.stream()
                .filter(
                        contentPartner -> contentPartner.getContentPartnerId().getValue().equals(request.getProviderId())
                )
                .findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, request.getProviderId()))
                .getName();

        val videoBuilder = Video.builder()
                .videoId(videoId)
                .title(request.getTitle())
                .description(request.getDescription())
                .releasedOn(request.getReleasedOn())
                .createdBy(createdBy)
                .contentPartnerId(request.getProviderId())
                .contentPartnerVideoId(request.getProviderVideoId())
                .playback(playback)
                .subjects(videoSubjects);

        if (useInternalProjection) {
            videoBuilder.type(request.getVideoType());
        }

        val video = videoBuilder.build();

        videos.put(videoId, video);
        playbacks.put(videoId, playback);
        createRequests.add(request);

        return videoId;
    }

    @Override
    public Boolean existsByContentPartnerInfo(String contentPartnerId, String contentPartnerVideoId) {
        return videos.values().stream().anyMatch(video ->
                video.getContentPartnerId().equals(contentPartnerId) &&
                        video.getContentPartnerVideoId().equals(contentPartnerVideoId)
        );
    }

    @Override
    public ContentPartnerId createContentPartner(CreateContentPartnerRequest request) {
        val newContentPartner = ContentPartner.builder()
                .contentPartnerId(
                        ContentPartnerId.builder()
                                .value(nextId())
                                .build()
                )
                .official(request.getAccreditedToYtChannelId() == null)
                .name(request.getName()).build();

        contentPartners.add(newContentPartner);
        return newContentPartner.getContentPartnerId();
    }

    @Override
    public ContentPartner findContentPartner(ContentPartnerId id) {
        return contentPartners.stream()
                .filter(contentPartner -> id.equals(contentPartner.getContentPartnerId()))
                .findFirst().orElse(null);
    }

    @Override
    public List<ContentPartner> findOfficialContentPartner(String name) {
        return contentPartners.stream()
                .filter(contentPartner -> name.equals(contentPartner.getName()) && contentPartner.getOfficial())
                .collect(toList());
    }

    @Override
    public List<ContentPartner> findContentPartnerByYoutubeChannelId(String youtubeChannelId) {
        return contentPartners.stream()
                .filter(contentPartner -> !contentPartner.getOfficial())
                .collect(Collectors.toList());
    }

    @Override
    public List<ContentPartner> getContentPartners() {
        return contentPartners;
    }

    @Override
    public Video get(VideoId id) {
        if (videos.containsKey(id)) {
            return videos.get(id);
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, id.getUri().toString());
        }
    }

    @Override
    @SneakyThrows
    public VideoId rawIdToVideoId(String rawId) {
        return new VideoId(new URI(String.format("%s/%s", "https://fake-video-service.com/videos", rawId)));
    }

    @Override
    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects.values());
    }

    @Override
    public List<Collection> getCollectionsDetailed(PageSpec pageSpec) {
        return detailedCollectionsByUser.values().stream().flatMap(java.util.Collection::stream).collect(toList());
    }

    @Override
    public List<Collection> getMyCollections(PageSpec pageSpec) {
        return getCollectionsByOwner("user@boclips.com", pageSpec);
    }

    @Override
    public List<Collection> getMyCollectionsDetailed(PageSpec pageSpec) {
        return trimToPageSize(getDetailedCollections("user@boclips.com"), pageSpec);
    }

    @Override
    public List<Collection> getCollectionsByOwner(String owner, PageSpec pageSpec) {
        return trimToPageSize(getCollections(owner), pageSpec);
    }

    @Override
    public CollectionId createCollection(CreateCollectionRequest request) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new InvalidCollectionRequestException("Title is required");
        }

        val collection = Collection.builder()
                .collectionId(rawIdToCollectionId(UUID.randomUUID().toString()))
                .title(request.getTitle())
                .description(request.getDescription())
                .videos(request.getVideos().stream().map(videoId -> get(rawIdToVideoId(videoId))).collect(toList()))
                .isPublic(request.isPublic())
                .mine(true)
                .subjects(
                        request.getSubjects().stream()
                                .map(subjectId -> subjects.get(new SubjectId(subjectId)))
                                .collect(toSet())
                )
                .build();
        this.collectionsByUser.put("", singletonList(collection));

        return collection.getCollectionId();
    }

    private List<Collection> trimToPageSize(List<Collection> collections, PageSpec pageSpec) {
        int pageSize = pageSpec.getPageSize() != null ? pageSpec.getPageSize() : 30;
        int itemsToRemove = Math.min(pageSize, collections.size());
        return Collections.unmodifiableList(collections.subList(0, itemsToRemove));
    }

    public void addCollection(Collection collection) {
        addCollection(collection, "user@boclips.com", true);
    }

    public void addCollection(Collection collection, String owner, boolean mine) {
        getCollections(owner).add(collection);
        getDetailedCollections(owner).add(Collection.builder()
                .collectionId(collection.getCollectionId())
                .subjects(collection.getSubjects())
                .title(collection.getTitle())
                .mine(mine)
                .videos(collection.getVideos().stream()
                        .map(video -> videos.get(video.getVideoId()))
                        .collect(toList())
                )
                .build()
        );
    }

    private List<Collection> getCollections(String user) {
        return collectionsByUser.computeIfAbsent(user, u -> new ArrayList<>());
    }

    private List<Collection> getDetailedCollections(String user) {
        return detailedCollectionsByUser.computeIfAbsent(user, u -> new ArrayList<>());
    }

    @Override
    public Collection get(CollectionId id) {
        return collectionsByUser.values().stream()
                .flatMap(java.util.Collection::stream)
                .filter(collection -> collection.getCollectionId().equals(id))
                .findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, id.getUri().toString()));
    }

    @Override
    public Collection getDetailed(CollectionId id) {
        Collection collection = get(id);

        return Collection.builder()
                .collectionId(collection.getCollectionId())
                .title(collection.getTitle())
                .subjects(collection.getSubjects())
                .videos(collection.getVideos().stream()
                        .map(video -> videos.get(video.getVideoId()))
                        .collect(toList())
                )
                .build();
    }

    @SneakyThrows
    public CollectionId rawIdToCollectionId(String rawId) {
        return new CollectionId(new URI(String.format("%s/%s", "https://fake-video-service.com/collections", rawId)));
    }

    private String nextId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24);
    }

    public List<CreateVideoRequest> getAllVideoRequests() {
        return createRequests;
    }

    public void clear() {
        createRequests.clear();
        videos.clear();
        illegalPlaybackIds.clear();
        subjects.clear();
        collectionsByUser.clear();
        detailedCollectionsByUser.clear();
        contentPartners.clear();
    }

    public void addIllegalPlaybackId(String playbackId) {
        illegalPlaybackIds.add(playbackId);
    }

    public Subject addSubject(String subjectName) {
        return addSubject(
                Subject.builder()
                        .id(SubjectId.builder().value(this.nextId()).build())
                        .name(subjectName)
                        .build()
        );
    }

    public void setUseInternalProjection(Boolean useInternalProjection) {
        this.useInternalProjection = useInternalProjection;
    }

    public Subject addSubject(Subject subject) {
        subjects.put(subject.getId(), subject);
        return subject;
    }

    public void createVideo(Video video) {
        this.videos.put(video.getVideoId(), video);
    }

    public void createContentPartner(ContentPartner contentPartner) {
        this.contentPartners.add(contentPartner);
    }
}
