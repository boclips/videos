package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.*;
import com.boclips.videos.service.client.exceptions.IllegalVideoRequestException;
import com.boclips.videos.service.client.exceptions.VideoExistsException;
import com.boclips.videos.service.client.exceptions.VideoNotFoundException;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URI;
import java.util.*;

public class FakeClient implements VideoServiceClient {

    private List<CreateVideoRequest> createRequests = new ArrayList<>();
    private Map<VideoId, Video> videos = new HashMap<>();
    private Set<String> illegalPlaybackIds = new HashSet<>();
    private Set<Subject> subjects = new HashSet<>();

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
                .subjects(request.getSubjects())
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
    public void setSubjects(VideoId id, Set<String> subjects) {
        videos.compute(id, (videoId, video) -> {
            if (video == null) throw new VideoNotFoundException(id);
            return video.toBuilder().subjects(subjects).build();
        });
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
        return new ArrayList(subjects);
    }

    private String nextId() {
        return UUID.randomUUID().toString();
    }

    public List<CreateVideoRequest> getAllVideoRequests() {
        return createRequests;
    }

    public void clear() {
        createRequests.clear();
        videos.clear();
        illegalPlaybackIds.clear();
    }

    public void addIllegalPlaybackId(String playbackId) {
        illegalPlaybackIds.add(playbackId);
    }

    public void addSubject(String subject) {
        subjects.add(Subject.builder()
                .id(UUID.randomUUID().toString())
                .name(subject)
                .build());
    }
}
