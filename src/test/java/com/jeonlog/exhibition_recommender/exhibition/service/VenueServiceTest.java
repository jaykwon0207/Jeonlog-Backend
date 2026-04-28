package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenuePhoto;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenueType;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueListResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenuePhotoRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;
    @Mock
    private VenuePhotoRepository venuePhotoRepository;
    @Mock
    private ExhibitionRepository exhibitionRepository;

    private VenueService venueService;

    @BeforeEach
    void setUp() {
        venueService = new VenueService(venueRepository, venuePhotoRepository, exhibitionRepository);
    }

    @Test
    void searchVenues_whenQueryIsBlank_throwsIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> venueService.searchVenues("   ", pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검색어를 입력해주세요.");
        verify(venueRepository, never())
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(any(), any(), any());
        verifyNoInteractions(venuePhotoRepository);
    }

    @Test
    void searchVenues_whenQueryIsValid_mapsVenueAndCoverImage() {
        Pageable pageable = PageRequest.of(0, 10);
        Venue venue = Venue.builder()
                .id(1L)
                .name("서울시립미술관")
                .type(VenueType.MUSEUM)
                .address("서울 중구")
                .logoImageUrl("https://example.com/logo.png")
                .build();

        VenuePhoto coverPhoto = VenuePhoto.builder()
                .id(10L)
                .venue(venue)
                .imageUrl("https://example.com/cover.png")
                .sortOrder(0)
                .isCover(true)
                .build();

        when(venueRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("서울"),
                eq("서울"),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(venue), pageable, 1));
        when(venuePhotoRepository.findFirstByVenue_IdAndIsCoverTrueOrderBySortOrderAscIdAsc(1L))
                .thenReturn(Optional.of(coverPhoto));

        Page<VenueListResponseDto> result = venueService.searchVenues(" 서울 ", pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(venueRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("서울"),
                eq("서울"),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("서울시립미술관");
        assertThat(result.getContent().get(0).getAddress()).isEqualTo("서울 중구");
        assertThat(result.getContent().get(0).getCoverImageUrl()).isEqualTo("https://example.com/cover.png");
        assertThat(result.getContent().get(0).getLogoImageUrl()).isEqualTo("https://example.com/logo.png");
    }
}
