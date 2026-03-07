package com.jeonlog.exhibition_recommender.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GoogleMobileLoginRequest {
    private String type;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private List<String> scopes;
        private String serverAuthCode;
        private String idToken;
        private User user;
    }

    @Getter
    @NoArgsConstructor
    public static class User {
        private String id;
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String photo;
    }
}
