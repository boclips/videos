package com.boclips.videos.service.client;

import com.boclips.videos.service.client.internal.ApiClient;
import com.boclips.videos.service.client.internal.FakeClient;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

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

    VideoId create(CreateVideoRequest request);

    Boolean existsByContentPartnerInfo(String contentPartnerId, String contentPartnerVideoId);

    void setSubjects(VideoId id, Set<String> subjects);

    Video get(VideoId id);

    VideoId rawIdToVideoId(String rawId);

    List<Subject> getSubjects();

    List<Collection> getMyCollections();
}
