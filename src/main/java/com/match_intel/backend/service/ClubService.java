package com.match_intel.backend.service;

import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.response.ClubAdminDto;
import com.match_intel.backend.dto.response.ClubCourtDto;
import com.match_intel.backend.dto.response.ClubDetailsDto;
import com.match_intel.backend.dto.response.ClubDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubCourtRepository;
import com.match_intel.backend.repository.ClubMemberRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ClubMemberRepository clubMemberRepository;
    @Autowired
    private ClubCourtRepository clubCourtRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailValidator emailValidator;

    @Transactional
    public ClubDto registerClub(String ownerUsername, String name, String address, String email,
                                String phone, String description, String logoUrl,
                                Double latitude, Double longitude, ReservationType reservationType) {
        if (name == null || name.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Club name is required");
        }
        if (address == null || address.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Club address is required");
        }
        if (clubRepository.findByName(name).isPresent()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Club name is already taken");
        }
        if (!emailValidator.validate(email)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        Club club = new Club();
        club.setName(name.trim());
        club.setAddress(address.trim());
        club.setEmail(email.trim());
        club.setPhone(phone);
        club.setDescription(description);
        club.setLogoUrl(logoUrl);
        club.setLatitude(latitude);
        club.setLongitude(longitude);
        club.setReservationType(reservationType != null ? reservationType : ReservationType.INSTANT);
        clubRepository.save(club);

        ClubMember ownerMember = new ClubMember();
        ownerMember.setClub(club);
        ownerMember.setUser(owner);
        ownerMember.setRole(ClubRole.OWNER);
        clubMemberRepository.save(ownerMember);

        return toDto(club, ownerUsername);
    }

    public List<ClubDto> getAllClubs(String requesterUsername) {
        return clubRepository.findAllByOrderByNameAsc().stream()
                .map(club -> toDto(club, requesterUsername))
                .toList();
    }

    public ClubDetailsDto getClubDetails(String requesterUsername, UUID clubId) {
        Club club = getClubEntity(clubId);

        List<ClubCourtDto> courts = clubCourtRepository.findByClubOrderByNameAsc(club).stream()
                .map(ClubService::toCourtDto)
                .toList();

        List<ClubAdminDto> admins = clubMemberRepository.findByClub(club).stream()
                .map(member -> new ClubAdminDto(
                        member.getId(),
                        member.getUser().getUsername(),
                        member.getUser().getFirstName(),
                        member.getUser().getLastName(),
                        member.getRole()
                ))
                .toList();

        return new ClubDetailsDto(toDto(club, requesterUsername), courts, admins);
    }

    public ClubDto updateClub(String requesterUsername, UUID clubId, Map<String, String> fields) {
        Club club = getClubEntity(clubId);
        if (!isClubAdmin(requesterUsername, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can update this club");
        }

        String newName = fields.get("name");
        if (newName != null && !newName.isBlank()) {
            clubRepository.findByName(newName.trim())
                    .filter(existing -> !existing.getId().equals(clubId))
                    .ifPresent(existing -> {
                        throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Club name is already taken");
                    });
            club.setName(newName.trim());
        }

        String address = fields.get("address");
        if (address != null && !address.isBlank()) {
            club.setAddress(address.trim());
        }

        String email = fields.get("email");
        if (email != null && !email.isBlank()) {
            if (!emailValidator.validate(email)) {
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid email format");
            }
            club.setEmail(email.trim());
        }

        if (fields.get("phone") != null && !fields.get("phone").isBlank()) {
            club.setPhone(fields.get("phone"));
        }
        if (fields.get("description") != null && !fields.get("description").isBlank()) {
            club.setDescription(fields.get("description"));
        }
        if (fields.get("logoUrl") != null && !fields.get("logoUrl").isBlank()) {
            club.setLogoUrl(fields.get("logoUrl"));
        }

        String latitude = fields.get("latitude");
        if (latitude != null && !latitude.isBlank()) {
            try {
                club.setLatitude(Double.parseDouble(latitude));
            } catch (NumberFormatException e) {
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid latitude");
            }
        }
        String longitude = fields.get("longitude");
        if (longitude != null && !longitude.isBlank()) {
            try {
                club.setLongitude(Double.parseDouble(longitude));
            } catch (NumberFormatException e) {
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid longitude");
            }
        }

        String reservationType = fields.get("reservationType");
        if (reservationType != null && !reservationType.isBlank()) {
            try {
                club.setReservationType(ReservationType.valueOf(reservationType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid reservation type");
            }
        }

        clubRepository.save(club);
        return toDto(club, requesterUsername);
    }

    public void addAdmin(String requesterUsername, UUID clubId, String newAdminUsername) {
        if (!isClubOwner(requesterUsername, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only the club owner can add admins");
        }

        Club club = getClubEntity(clubId);
        User newAdmin = userRepository.findByUsername(newAdminUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        if (clubMemberRepository.findByClubAndUser(club, newAdmin).isPresent()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "User is already a club admin");
        }

        ClubMember member = new ClubMember();
        member.setClub(club);
        member.setUser(newAdmin);
        member.setRole(ClubRole.ADMIN);
        clubMemberRepository.save(member);
    }

    public void removeAdmin(String requesterUsername, UUID clubId, String adminUsername) {
        if (!isClubOwner(requesterUsername, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only the club owner can remove admins");
        }

        Club club = getClubEntity(clubId);
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        ClubMember member = clubMemberRepository.findByClubAndUser(club, admin)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User is not a club admin"));

        if (member.getRole() == ClubRole.OWNER) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "The club owner cannot be removed");
        }
        if (member.getUser().getUsername().equals(requesterUsername)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "The club owner cannot remove themselves");
        }

        clubMemberRepository.delete(member);
    }

    public List<ClubAdminDto> getAdmins(UUID clubId) {
        Club club = getClubEntity(clubId);
        return clubMemberRepository.findByClub(club).stream()
                .map(member -> new ClubAdminDto(
                        member.getId(),
                        member.getUser().getUsername(),
                        member.getUser().getFirstName(),
                        member.getUser().getLastName(),
                        member.getRole()
                ))
                .toList();
    }

    public ClubCourtDto addCourt(String requesterUsername, UUID clubId, String name,
                                 SurfaceType surfaceType, BigDecimal pricePerHour) {
        if (!isClubAdmin(requesterUsername, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can add courts");
        }
        if (name == null || name.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Court name is required");
        }
        if (surfaceType == null) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Surface type is required");
        }
        if (pricePerHour == null || pricePerHour.signum() < 0) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid price per hour");
        }

        Club club = getClubEntity(clubId);
        ClubCourt court = new ClubCourt();
        court.setClub(club);
        court.setName(name.trim());
        court.setSurfaceType(surfaceType);
        court.setPricePerHour(pricePerHour);
        clubCourtRepository.save(court);

        return toCourtDto(court);
    }

    public List<ClubCourtDto> getCourts(UUID clubId) {
        Club club = getClubEntity(clubId);
        return clubCourtRepository.findByClubOrderByNameAsc(club).stream()
                .map(ClubService::toCourtDto)
                .toList();
    }

    public void removeCourt(String requesterUsername, UUID clubId, UUID courtId) {
        if (!isClubAdmin(requesterUsername, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can remove courts");
        }
        ClubCourt court = clubCourtRepository.findById(courtId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Court not found"));
        if (!court.getClub().getId().equals(clubId)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Court does not belong to this club");
        }
        clubCourtRepository.delete(court);
    }

    public boolean isClubAdmin(String username, UUID clubId) {
        if (username == null) {
            return false;
        }
        Club club = getClubEntity(clubId);
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return false;
        }
        return clubMemberRepository.findByClubAndUser(club, user)
                .map(member -> member.getRole() == ClubRole.OWNER || member.getRole() == ClubRole.ADMIN)
                .orElse(false);
    }

    public boolean isClubOwner(String username, UUID clubId) {
        if (username == null) {
            return false;
        }
        Club club = getClubEntity(clubId);
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return false;
        }
        return clubMemberRepository.findByClubAndUser(club, user)
                .map(member -> member.getRole() == ClubRole.OWNER)
                .orElse(false);
    }

    public Club getClubEntity(UUID clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));
    }

    public ClubDto toDto(Club club, String requesterUsername) {
        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getAddress(),
                club.getEmail(),
                club.getPhone(),
                club.getDescription(),
                club.getLogoUrl(),
                club.getLatitude(),
                club.getLongitude(),
                club.getReservationType(),
                club.getAverageRating(),
                club.getNumberOfReviews(),
                0,
                false,
                isClubAdmin(requesterUsername, club.getId()),
                club.getCreatedAt()
        );
    }

    private static ClubCourtDto toCourtDto(ClubCourt court) {
        return new ClubCourtDto(
                court.getId(),
                court.getName(),
                court.getSurfaceType(),
                court.getPricePerHour()
        );
    }
}
