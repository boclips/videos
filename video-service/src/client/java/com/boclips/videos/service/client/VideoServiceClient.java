package com.boclips.videos.service.client;

import com.boclips.videos.service.client.internal.ApiClient;
import com.boclips.videos.service.client.internal.FakeClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public interface VideoServiceClient {

    static FakeClient getFakeClient() {
        return new FakeClient();
    }

    static VideoServiceClient getApiClient(String baseUrl, ServiceCredentials serviceCredentials) {
        ClientCredentialsResourceDetails credentials = new ClientCredentialsResourceDetails();
        credentials.setAccessTokenUri(serviceCredentials.getAccessTokenUri());
        credentials.setClientId(serviceCredentials.getClientId());
        credentials.setClientSecret(serviceCredentials.getClientSecret());

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(credentials);
        return new ApiClient(baseUrl, restTemplate);
    }

    static VideoServiceClient getUnauthorisedApiClient(String baseUrl) {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor("user@boclips.com", "reallySecurePassword123"));
        return new ApiClient(baseUrl, restTemplate);
    }

    VideoId createVideo(CreateVideoRequest request);

    Boolean existsByContentPartnerInfo(String contentPartnerId, String contentPartnerVideoId);

    ContentPartnerId createContentPartner(CreateContentPartnerRequest request);

    List<ContentPartner> findOfficialContentPartner(String name);

    List<ContentPartner> findContentPartnerByYoutubeChannelId(String youtubeChannelId);

    Video get(VideoId id);

    VideoId rawIdToVideoId(String rawId);

    List<Subject> getSubjects();

    default List<Collection> getCollectionsDetailed() {
        return getCollectionsDetailed(new PageSpec());
    }

    List<Collection> getCollectionsDetailed(PageSpec pageSpec);

    default List<Collection> getMyCollections() {
        return getMyCollections(new PageSpec());
    }

    List<Collection> getMyCollections(PageSpec pageSpec);

    default List<Collection> getMyCollectionsDetailed() {
        return getMyCollectionsDetailed(new PageSpec());
    }

    List<Collection> getMyCollectionsDetailed(PageSpec pageSpec);

    default List<Collection> getCollectionsByOwner(String owner) {
        return getCollectionsByOwner(owner, new PageSpec());
    }

    List<Collection> getCollectionsByOwner(String owner, PageSpec pageSpec);

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class PageSpec {
        private Integer pageSize;
    }

    Collection get(CollectionId id);

    Collection getDetailed(CollectionId id);

    CollectionId rawIdToCollectionId(String rawId);
}
