package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.VenuePhoto;
import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VenuePhotoDto implements Serializable {
    private Long id;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
    private boolean isCover;

    public static VenuePhotoDto from(VenuePhoto p) {
        return VenuePhotoDto.builder()
                .id(p.getId())
                .imageUrl(p.getImageUrl())
                .caption(p.getCaption())
                .sortOrder(p.getSortOrder())
                .isCover(p.isCover())
                .build();
    }
}
