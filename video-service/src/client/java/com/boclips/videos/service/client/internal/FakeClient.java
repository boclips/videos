package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.Collection;
import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class FakeClient implements VideoServiceClient {

    private List<CreateVideoRequest> createRequests = new ArrayList<>();
    private Map<VideoId, Video> videos = new HashMap<>();
    private Set<String> illegalPlaybackIds = new HashSet<>();
    private Map<VideoId, Playback> playbacks = new HashMap<>();
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
        Playback playback = Playback.builder()
                .playbackId(request.getPlaybackId())
                .duration(Duration.ofMinutes(7))
                .thumbnailUrl("https://thumbnailz.org/img/" + nextId())
                .build();
        val video = Video.builder()
                .videoId(videoId)
                .title(request.getTitle())
                .description(request.getDescription())
                .contentPartnerId(request.getProvider())
                .contentPartnerVideoId(request.getProviderVideoId())
                .playback(playback)
                .build();
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
