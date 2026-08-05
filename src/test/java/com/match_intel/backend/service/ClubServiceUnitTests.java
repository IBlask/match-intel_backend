package com.match_intel.backend.service;

import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.response.ClubCourtDto;
import com.match_intel.backend.dto.response.ClubDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClubServiceUnitTests {

    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubMemberRepository clubMemberRepository;
    @Mock
    private ClubCourtRepository clubCourtRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private ClubFollowRepository clubFollowRepository;
    @Mock
    private ClubFollowService clubFollowService;
    @InjectMocks
    private ClubService clubService;

    private User owner;
    private User admin;
    private User stranger;
    private Club club;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = new User("owner", "Owner", "One", "owner@example.com", "pass123");
        admin = new User("admin", "Admin", "One", "admin@example.com", "pass123");
        stranger = new User("stranger", "Str", "Anger", "stranger@example.com", "pass123");

        club = new Club();
        club.setId(UUID.randomUUID());
        club.setName("Test Club");
        club.setAddress("Test Street 1");
        club.setEmail("club@example.com");
    }

    @Test
    @DisplayName("registerClub - creates club with OWNER member")
    void registerClub_shouldCreateOwnerMember() {
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(clubRepository.findByName("New Club")).thenReturn(Optional.empty());
        when(emailValidator.validate("new@example.com")).thenReturn(true);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> {
            Club saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
        when(clubRepository.findById(any(UUID.class))).thenReturn(Optional.of(club));

        ClubDto dto = clubService.registerClub(
                "owner", "New Club", "Street 1", "new@example.com",
                null, null, null, null, null, ReservationType.INSTANT
        );

        assertNotNull(dto);
        ArgumentCaptor<ClubMember> memberCaptor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(memberCaptor.capture());
        assertEquals(ClubRole.OWNER, memberCaptor.getValue().getRole());
        assertEquals("owner", memberCaptor.getValue().getUser().getUsername());
    }

    @Test
    @DisplayName("registerClub - duplicate name throws 400")
    void registerClub_shouldRejectDuplicateName() {
        when(clubRepository.findByName("Test Club")).thenReturn(Optional.of(club));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.registerClub(
                        "owner", "Test Club", "Street 1", "new@example.com",
                        null, null, null, null, null, ReservationType.INSTANT
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubRepository, never()).save(any(Club.class));
    }

    @Test
    @DisplayName("registerClub - invalid email throws 400")
    void registerClub_shouldRejectInvalidEmail() {
        when(clubRepository.findByName("New Club")).thenReturn(Optional.empty());
        when(emailValidator.validate("not-an-email")).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.registerClub(
                        "owner", "New Club", "Street 1", "not-an-email",
                        null, null, null, null, null, ReservationType.INSTANT
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubRepository, never()).save(any(Club.class));
    }

    @Test
    @DisplayName("updateClub - admin can update, stranger gets 403")
    void updateClub_shouldAllowAdminAndRejectStranger() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndUser(club, admin))
                .thenReturn(Optional.of(member(admin, ClubRole.ADMIN)));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(clubMemberRepository.findByClubAndUser(club, stranger))
                .thenReturn(Optional.empty());
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubDto dto = clubService.updateClub(
                "admin", club.getId(), java.util.Map.of("description", "New description")
        );

        assertEquals("New description", club.getDescription());
        assertNotNull(dto);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.updateClub(
                        "stranger", club.getId(), java.util.Map.of("description", "Hacked")
                ));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("addAdmin - owner only, stranger gets 403")
    void addAdmin_shouldBeOwnerOnly() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(clubMemberRepository.findByClubAndUser(club, owner))
                .thenReturn(Optional.of(member(owner, ClubRole.OWNER)));
        when(clubMemberRepository.findByClubAndUser(club, stranger))
                .thenReturn(Optional.of(member(stranger, ClubRole.ADMIN)));
        when(userRepository.findByUsername("newadmin")).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndUser(club, admin))
                .thenReturn(Optional.empty());

        clubService.addAdmin("owner", club.getId(), "newadmin");
        verify(clubMemberRepository).save(argThat(m -> m.getRole() == ClubRole.ADMIN));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.addAdmin("stranger", club.getId(), "newadmin"));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(clubMemberRepository, times(1)).save(any(ClubMember.class));
    }

    @Test
    @DisplayName("removeAdmin - owner cannot be removed")
    void removeAdmin_shouldNotAllowRemovingOwner() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(clubMemberRepository.findByClubAndUser(club, owner))
                .thenReturn(Optional.of(member(owner, ClubRole.OWNER)));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndUser(club, admin))
                .thenReturn(Optional.of(member(admin, ClubRole.OWNER)));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.removeAdmin("owner", club.getId(), "admin"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubMemberRepository, never()).delete(any(ClubMember.class));
    }

    @Test
    @DisplayName("addCourt - admin only, stranger gets 403")
    void addCourt_shouldBeAdminOnly() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndUser(club, admin))
                .thenReturn(Optional.of(member(admin, ClubRole.ADMIN)));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(clubMemberRepository.findByClubAndUser(club, stranger))
                .thenReturn(Optional.empty());
        when(clubCourtRepository.save(any(ClubCourt.class))).thenAnswer(invocation -> {
            ClubCourt court = invocation.getArgument(0);
            court.setId(UUID.randomUUID());
            return court;
        });

        ClubCourtDto dto = clubService.addCourt(
                "admin", club.getId(), "Court 1", SurfaceType.HARD, new BigDecimal("12.00")
        );

        assertEquals("Court 1", dto.name());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubService.addCourt(
                        "stranger", club.getId(), "Court 2", SurfaceType.HARD, new BigDecimal("12.00")
                ));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("getCourts - returns courts ordered by name")
    void getCourts_shouldReturnOrderedCourts() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        ClubCourt courtB = court("Court B");
        ClubCourt courtA = court("Court A");
        when(clubCourtRepository.findByClubOrderByNameAsc(club)).thenReturn(List.of(courtA, courtB));

        List<ClubCourtDto> courts = clubService.getCourts(club.getId());

        assertEquals(2, courts.size());
        assertEquals("Court A", courts.get(0).name());
        assertEquals("Court B", courts.get(1).name());
    }

    private ClubMember member(User user, ClubRole role) {
        ClubMember member = new ClubMember();
        member.setClub(club);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    private ClubCourt court(String name) {
        ClubCourt court = new ClubCourt();
        court.setId(UUID.randomUUID());
        court.setClub(club);
        court.setName(name);
        court.setSurfaceType(SurfaceType.HARD);
        court.setPricePerHour(new BigDecimal("12.00"));
        return court;
    }
}
