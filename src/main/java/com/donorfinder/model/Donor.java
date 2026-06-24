package com.donorfinder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    private String city;
    private String state;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Boolean availableForBlood = true;

    @Column(nullable = false)
    private Boolean availableForOrgan = false;

    private String organsWillingToDonate;

    private Integer age;

    // Last blood donation date — enforces 3-month cooldown
    private LocalDate lastDonatedBlood;

    // Whether donor passed the eligibility questionnaire
    @Column(nullable = false)
    private Boolean eligibilityCleared = false;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    public enum BloodGroup {
        A_POSITIVE("A+"), A_NEGATIVE("A-"),
        B_POSITIVE("B+"), B_NEGATIVE("B-"),
        O_POSITIVE("O+"), O_NEGATIVE("O-"),
        AB_POSITIVE("AB+"), AB_NEGATIVE("AB-");

        private final String label;
        BloodGroup(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}
