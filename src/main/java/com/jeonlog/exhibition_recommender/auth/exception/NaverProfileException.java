package com.jeonlog.exhibition_recommender.auth.exception;

public class NaverProfileException extends RuntimeException {

    private final String code;
    private final boolean upstream;

    private NaverProfileException(String code, String message, boolean upstream) {
        super(message);
        this.code = code;
        this.upstream = upstream;
    }

    public static NaverProfileException badRequest(String code, String message) {
        return new NaverProfileException(code, message, false);
    }

    public static NaverProfileException upstream(String code, String message) {
        return new NaverProfileException(code, message, true);
    }

    public String getCode() {
        return code;
    }

    public boolean isUpstream() {
        return upstream;
    }
}
