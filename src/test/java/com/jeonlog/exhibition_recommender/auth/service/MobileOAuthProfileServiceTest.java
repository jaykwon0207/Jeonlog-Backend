package com.jeonlog.exhibition_recommender.auth.service;

import com.jeonlog.exhibition_recommender.auth.exception.NaverProfileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileOAuthProfileServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private MobileOAuthProfileService mobileOAuthProfileService;

    @BeforeEach
    void setUp() {
        mobileOAuthProfileService = new MobileOAuthProfileService(webClient);
    }

    @Test
    void fetchNaverProfile_withValidResponse_returnsProfile() {
        stubNaverProfileResponse(Map.of(
                "resultcode", "00",
                "response", Map.of(
                        "id", "naver-id",
                        "email", "n@e.com",
                        "name", "Naver User"
                )
        ));

        MobileOAuthProfileService.NaverProfile profile = mobileOAuthProfileService.fetchNaverProfile("access-token");

        assertThat(profile.id()).isEqualTo("naver-id");
        assertThat(profile.email()).isEqualTo("n@e.com");
        assertThat(profile.name()).isEqualTo("Naver User");
    }

    @Test
    void fetchNaverProfile_withNonSuccessResultCode_throwsUpstreamException() {
        stubNaverProfileResponse(Map.of(
                "resultcode", "024",
                "message", "Authentication failed"
        ));

        assertThatThrownBy(() -> mobileOAuthProfileService.fetchNaverProfile("access-token"))
                .isInstanceOf(NaverProfileException.class)
                .hasMessage("NAVER_PROFILE_FETCH_FAILED")
                .extracting(e -> ((NaverProfileException) e).getCode())
                .isEqualTo("NAVER_PROFILE_FETCH_FAILED");
    }

    @Test
    void fetchNaverProfile_withMissingId_throwsUpstreamException() {
        stubNaverProfileResponse(Map.of(
                "resultcode", "00",
                "response", Map.of(
                        "email", "n@e.com",
                        "name", "Naver User"
                )
        ));

        assertThatThrownBy(() -> mobileOAuthProfileService.fetchNaverProfile("access-token"))
                .isInstanceOf(NaverProfileException.class)
                .hasMessage("NAVER_ID_MISSING")
                .extracting(e -> ((NaverProfileException) e).getCode())
                .isEqualTo("NAVER_ID_MISSING");
    }

    @Test
    void fetchNaverProfile_withBlankToken_throwsBadRequestExceptionWithoutCallingUpstream() {
        assertThatThrownBy(() -> mobileOAuthProfileService.fetchNaverProfile(" "))
                .isInstanceOf(NaverProfileException.class)
                .hasMessage("NAVER_ACCESS_TOKEN_REQUIRED")
                .extracting(e -> ((NaverProfileException) e).isUpstream())
                .isEqualTo(false);

        verifyNoInteractions(webClient);
    }

    @SuppressWarnings("unchecked")
    private void stubNaverProfileResponse(Map<String, Object> body) {
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://openapi.naver.com/v1/nid/me"))
                .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString()))
                .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(body));
    }
}
