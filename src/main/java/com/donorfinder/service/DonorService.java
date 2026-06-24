package com.donorfinder.service;

import com.donorfinder.dto.DonorDto;
import com.donorfinder.exception.BadRequestException;
import com.donorfinder.exception.ResourceNotFoundException;
import com.donorfinder.model.Donor;
import com.donorfinder.model.User;
import com.donorfinder.repository.DonorRepository;
import com.donorfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;

    @Value("${app.donor.search-radius-km:50}")
    private Double defaultRadiusKm;

    public DonorDto.ProfileResponse createOrUpdateProfile(String email, DonorDto.ProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate 3-month cooldown
        if (req.getLastDonatedBlood() != null) {
            LocalDate nextEligible = req.getLastDonatedBlood().plusMonths(3);
            if (nextEligible.isAfter(LocalDate.now())) {
                throw new BadRequestException(
                    "You are not eligible to donate blood yet. Your next eligible date is: " + nextEligible
                );
            }
        }

        // Validate eligibility questionnaire
        boolean eligibilityCleared = false;
        if (req.getEligibilityAnswers() != null) {
            eligibilityCleared = validateEligibility(req.getEligibilityAnswers());
        }

        Donor donor = donorRepository.findByUserId(user.getId())
                .orElse(Donor.builder().user(user).build());

        donor.setBloodGroup(req.getBloodGroup());
        donor.setCity(req.getCity());
        donor.setState(req.getState());
        donor.setLatitude(req.getLatitude());
        donor.setLongitude(req.getLongitude());
        donor.setAvailableForBlood(req.getAvailableForBlood() != null ? req.getAvailableForBlood() : true);
        donor.setAvailableForOrgan(req.getAvailableForOrgan() != null ? req.getAvailableForOrgan() : false);
        donor.setOrgansWillingToDonate(req.getOrgansWillingToDonate());
        donor.setAge(req.getAge());
        donor.setLastDonatedBlood(req.getLastDonatedBlood());
        donor.setEligibilityCleared(eligibilityCleared);

        donorRepository.save(donor);
        return DonorDto.ProfileResponse.from(donor);
    }

    public DonorDto.EligibilityCheckResponse checkEligibility(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        DonorDto.EligibilityCheckResponse res = new DonorDto.EligibilityCheckResponse();

        if (!Boolean.TRUE.equals(donor.getEligibilityCleared())) {
            res.setEligible(false);
            res.setReason("You have not completed the eligibility questionnaire yet.");
            return res;
        }

        if (donor.getLastDonatedBlood() != null) {
            LocalDate nextEligible = donor.getLastDonatedBlood().plusMonths(3);
            if (nextEligible.isAfter(LocalDate.now())) {
                res.setEligible(false);
                res.setReason("You donated blood recently. The minimum gap between donations is 3 months.");
                res.setNextEligibleDate(nextEligible);
                return res;
            }
        }

        res.setEligible(true);
        res.setReason("You are eligible to donate blood.");
        return res;
    }

    public DonorDto.ProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not set up yet"));

        return DonorDto.ProfileResponse.from(donor);
    }

    public List<DonorDto.ProfileResponse> searchBloodDonors(DonorDto.SearchRequest req) {
        if (req.getBloodGroup() == null) {
            throw new BadRequestException("Blood group is required for blood donor search");
        }
        double radius = req.getRadiusKm() != null ? req.getRadiusKm() : defaultRadiusKm;

        return donorRepository.findNearbyBloodDonors(
                req.getBloodGroup().name(), req.getLatitude(), req.getLongitude(), radius
        ).stream().map(d -> {
            DonorDto.ProfileResponse profile = DonorDto.ProfileResponse.from(d);
            profile.setDistanceKm(haversineKm(req.getLatitude(), req.getLongitude(), d.getLatitude(), d.getLongitude()));
            return profile;
        }).toList();
    }

    public List<DonorDto.ProfileResponse> searchOrganDonors(DonorDto.SearchRequest req) {
        if (req.getOrganNeeded() == null || req.getOrganNeeded().isBlank()) {
            throw new BadRequestException("Organ name is required for organ donor search");
        }
        double radius = req.getRadiusKm() != null ? req.getRadiusKm() : defaultRadiusKm;

        return donorRepository.findNearbyOrganDonors(
                req.getOrganNeeded().toUpperCase(), req.getLatitude(), req.getLongitude(), radius
        ).stream().map(d -> {
            DonorDto.ProfileResponse profile = DonorDto.ProfileResponse.from(d);
            profile.setDistanceKm(haversineKm(req.getLatitude(), req.getLongitude(), d.getLatitude(), d.getLongitude()));
            return profile;
        }).toList();
    }

    public DonorDto.ProfileResponse toggleAvailability(String email, boolean forBlood, boolean available) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        // Can't mark available if not eligible
        if (available && forBlood) {
            if (!Boolean.TRUE.equals(donor.getEligibilityCleared())) {
                throw new BadRequestException("Complete the eligibility questionnaire before marking yourself available.");
            }
            if (donor.getLastDonatedBlood() != null) {
                LocalDate nextEligible = donor.getLastDonatedBlood().plusMonths(3);
                if (nextEligible.isAfter(LocalDate.now())) {
                    throw new BadRequestException("You are within the 3-month cooldown period. Next eligible: " + nextEligible);
                }
            }
        }

        if (forBlood) donor.setAvailableForBlood(available);
        else donor.setAvailableForOrgan(available);

        donorRepository.save(donor);
        return DonorDto.ProfileResponse.from(donor);
    }

    private boolean validateEligibility(DonorDto.EligibilityAnswers a) {
        return a.isNoRecentSurgery()
                && a.isNoRecentTattoo()
                && a.isNoChronicIllness()
                && a.isNotPregnantOrNursing()
                && a.isNoRecentAlcohol()
                && a.isNoRecentMedication()
                && a.isWeightAbove50kg()
                && a.isNoRecentMalaria()
                && a.isNoPreviousTransfusionIssues();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return Math.round(6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) * 100.0) / 100.0;
    }
}
