package com.jeonlog.exhibition_recommender.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderAwareAuthorizationRequestResolverTest {

    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String CODE_VERIFIER = "code_verifier";

    private ProviderAwareAuthorizationRequestResolver resolver;

    @BeforeEach
    void setUp() {
        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .clientId("google-client")
                .clientSecret("google-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .scope("profile", "email")
                .build();

        ClientRegistration naver = ClientRegistration.withRegistrationId("naver")
                .clientId("naver-client")
                .clientSecret("naver-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .scope("name", "email")
                .build();

        resolver = new ProviderAwareAuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(google, naver)
        );
    }

    @Test
    void resolve_google_addsPromptAndPkce() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/oauth2/authorization/google");
        request.setMethod("GET");

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters())
                .containsEntry("prompt", "select_account")
                .containsEntry(CODE_CHALLENGE_METHOD, "S256")
                .containsKey(CODE_CHALLENGE);
        assertThat(authRequest.getAttributes()).containsKey(CODE_VERIFIER);
    }

    @Test
    void resolve_naver_addsAuthTypeAndPkce() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/oauth2/authorization/naver");
        request.setMethod("GET");

        OAuth2AuthorizationRequest authRequest = resolver.resolve(request);

        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getAdditionalParameters())
                .containsEntry("auth_type", "reauthenticate")
                .containsEntry(CODE_CHALLENGE_METHOD, "S256")
                .containsKey(CODE_CHALLENGE);
        assertThat(authRequest.getAttributes()).containsKey(CODE_VERIFIER);
    }
}
