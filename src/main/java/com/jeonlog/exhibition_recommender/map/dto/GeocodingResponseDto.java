package com.jeonlog.exhibition_recommender.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GeocodingResponseDto {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("meta")
    private Meta meta;
    
    @JsonProperty("addresses")
    private List<Address> addresses;
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("totalCount")
        private int totalCount;
        
        @JsonProperty("page")
        private int page;
        
        @JsonProperty("count")
        private int count;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Address {
        @JsonProperty("roadAddress")
        private String roadAddress;
        
        @JsonProperty("jibunAddress")
        private String jibunAddress;
        
        @JsonProperty("englishAddress")
        private String englishAddress;
        
        @JsonProperty("x")
        private String x;  // 경도
        
        @JsonProperty("y")
        private String y;  // 위도
        
        @JsonProperty("distance")
        private double distance;
    }
}
