package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.Collection;
import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URI;
import java.util.*;

public class FakeClient implements VideoServiceClient {

    private List<CreateVideoRequest> createRequests = new ArrayList<>();
    private Map<VideoId, Video> videos = new HashMap<>();
    private Set<String> illegalPlaybackIds = new HashSet<>();
    private Set<Subject> subjects = new HashSet<>();
    private Map<String, List<Collection>> collectionsByUser = new HashMap<>();

    @Override
    public VideoId create(CreateVideoRequest request) {
        if (existsByContentPartnerInfo(request.getProvider(), request.getProviderVideoId())) {
            throw new VideoExistsException();
        }

        if (illegalPlaybackIds.contains(request.getPlaybackId())) {
            throw new IllegalVideoRequestException();
        }

        val videoId = rawIdToVideoId(nextId());
        val video = Video.builder()
                .videoId(videoId)
                .title(request.getTitle())
                .description(request.getDescription())
                .contentPartnerId(request.getProvider())
                .contentPartnerVideoId(request.getProviderVideoId())
                .build();
        videos.put(videoId, video);
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
    public Video get(VideoId id) {
        return videos.get(id);
    }

    @Override
    @SneakyThrows
    public VideoId rawIdToVideoId(String rawId) {
        return new VideoId(new URI(String.format("%s/%s", "https://fake-video-service.com/videos", rawId)));
    }

    @Override
    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects);
    }

    @Override
    public List<Collection> getMyCollections(PageSpec pageSpec) {
        return getCollectionsByOwner("user@boclips.com", pageSpec);
    }

    @Override
    public List<Collection> getCollectionsByOwner(String owner, PageSpec pageSpec) {
        List<Collection> allUserCollections = getCollections(owner);

        int pageSize = pageSpec.getPageSize() != null ? pageSpec.getPageSize() : 30;
        int itemsToRemove = Math.min(pageSize, allUserCollections.size());
        return Collections.unmodifiableList(allUserCollections.subList(0, itemsToRemove));
    }

    public void addCollection(Collection collection) {
        addCollection(collection, "user@boclips.com");
    }

    public void addCollection(Collection collection, String owner) {
        getCollections(owner).add(collection);
    }

    private List<Collection> getCollections(String user) {
        return collectionsByUser.computeIfAbsent(user, u -> new ArrayList<>());
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

    public Subject addSubject(Subject subject) {
        subjects.add(subject);
        return subject;
    }
}
