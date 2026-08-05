package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.ClubReviewDto;
import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubMember;
import com.match_intel.backend.entity.ClubReview;
import com.match_intel.backend.entity.ClubRole;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubMemberRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.ClubReviewRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClubReviewService {

    @Autowired
    private ClubReviewRepository clubReviewRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Transactional
    public ClubReviewDto addOrUpdateReview(String username, UUID clubId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        ClubMember membership = clubMemberRepository.findByClubAndUser(club, user).orElse(null);
        if (membership != null && membership.getRole() == ClubRole.OWNER) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "The club owner cannot review their own club");
        }

        ClubReview review = clubReviewRepository.findByClubAndUser(club, user).orElse(null);
        if (review == null) {
            review = new ClubReview();
            review.setClub(club);
            review.setUser(user);
        }
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        clubReviewRepository.save(review);

        recomputeAverage(club);
        return toDto(review);
    }

    public List<ClubReviewDto> getReviews(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));
        return clubReviewRepository.findByClubOrderByCreatedAtDesc(club).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteOwnReview(String username, UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        ClubReview review = clubReviewRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Review not found"));

        clubReviewRepository.delete(review);
        recomputeAverage(club);
    }

    private void recomputeAverage(Club club) {
        List<ClubReview> reviews = clubReviewRepository.findByClubOrderByCreatedAtDesc(club);
        if (reviews.isEmpty()) {
            club.setAverageRating(null);
            club.setNumberOfReviews(0);
        } else {
            double sum = reviews.stream().mapToInt(ClubReview::getRating).sum();
            double average = Math.round((sum / reviews.size()) * 10.0) / 10.0;
            club.setAverageRating(average);
            club.setNumberOfReviews(reviews.size());
        }
        clubRepository.save(club);
    }

    private ClubReviewDto toDto(ClubReview review) {
        return new ClubReviewDto(
                review.getId(),
                review.getClub().getId(),
                review.getUser().getUsername(),
                review.getUser().getFirstName(),
                review.getUser().getLastName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
