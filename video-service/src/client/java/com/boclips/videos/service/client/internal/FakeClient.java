package com.boclips.videos.service.client.internal;

import com.boclips.videos.service.client.CreateVideoRequest;
import com.boclips.videos.service.client.VideoId;
import com.boclips.videos.service.client.VideoServiceClient;
import com.boclips.videos.service.client.exceptions.VideoNotFoundException;
import com.boclips.videos.service.client.spring.Video;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FakeClient implements VideoServiceClient {

    private Map<VideoId, Video> videos = new HashMap<>();

    @Override
    @SneakyThrows
    public VideoId create(CreateVideoRequest request) {
        val videoId = new VideoId(new URI(String.format("https://blah.com/videos/%s", UUID.randomUUID())));
        val video = Video.builder()
                .videoId(videoId)
                .subjects(request.getSubjects())
                .contentPartnerId(request.getProvider())
                .contentPartnerVideoId(request.getProviderVideoId())
                .build();
        videos.put(videoId, video);
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
}
