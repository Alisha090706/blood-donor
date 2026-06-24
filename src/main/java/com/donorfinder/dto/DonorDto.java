package com.donorfinder.dto;

import com.donorfinder.model.Donor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

public class DonorDto {

    @Data
    public static class ProfileRequest {
        @NotNull(message = "Blood group is required")
        private Donor.BloodGroup bloodGroup;

        private String city;
        private String state;

        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private Boolean availableForBlood = true;
        private Boolean availableForOrgan = false;
        private String organsWillingToDonate;

        @Min(18) @Max(65)
        private Integer age;

        // Date of last blood donation — nullable if never donated
        private LocalDate lastDonatedBlood;

        // Eligibility questionnaire answers
        private EligibilityAnswers eligibilityAnswers;
    }

    @Data
    public static class EligibilityAnswers {
        private boolean noRecentSurgery;          // No surgery in last 6 months
        private boolean noRecentTattoo;           // No tattoo/piercing in last 12 months
        private boolean noChronicIllness;         // No diabetes, hepatitis, HIV, cancer
        private boolean notPregnantOrNursing;     // Not pregnant or breastfeeding
        private boolean noRecentAlcohol;          // No alcohol in last 24 hours
        private boolean noRecentMedication;       // No antibiotics or blood thinners
        private boolean weightAbove50kg;          // Weight >= 50 kg
        private boolean noRecentMalaria;          // No malaria in last 3 months
        private boolean noPreviousTransfusionIssues; // No adverse reactions to past donations
    }

    @Data
    public static class ProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String bloodGroup;
        private String city;
        private String state;
        private Boolean availableForBlood;
        private Boolean availableForOrgan;
        private String organsWillingToDonate;
        private Integer age;
        private Double distanceKm;
        private Boolean eligibilityCleared;
        private LocalDate lastDonatedBlood;
        private Boolean eligibleToDonatNow;

        public static ProfileResponse from(Donor d) {
            ProfileResponse r = new ProfileResponse();
            r.id = d.getId();
            r.name = d.getUser().getName();
            r.email = d.getUser().getEmail();
            r.phone = d.getUser().getPhone();
            r.bloodGroup = d.getBloodGroup().getLabel();
            r.city = d.getCity();
            r.state = d.getState();
            r.availableForBlood = d.getAvailableForBlood();
            r.availableForOrgan = d.getAvailableForOrgan();
            r.organsWillingToDonate = d.getOrgansWillingToDonate();
            r.age = d.getAge();
            r.eligibilityCleared = d.getEligibilityCleared();
            r.lastDonatedBlood = d.getLastDonatedBlood();
            r.eligibleToDonatNow = isEligibleNow(d);
            return r;
        }

        private static boolean isEligibleNow(Donor d) {
            if (!Boolean.TRUE.equals(d.getEligibilityCleared())) return false;
            if (d.getLastDonatedBlood() == null) return true;
            return d.getLastDonatedBlood().plusMonths(3).isBefore(LocalDate.now());
        }
    }

    @Data
    public static class SearchRequest {
        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        private Double radiusKm = 50.0;
        private Donor.BloodGroup bloodGroup;
        private String organNeeded;
    }

    @Data
    public static class EligibilityCheckResponse {
        private boolean eligible;
        private String reason;
        private LocalDate nextEligibleDate;
    }
}
