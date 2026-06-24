package com.donorfinder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "urgent_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UrgentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    // For blood requests
    @Enumerated(EnumType.STRING)
    private Donor.BloodGroup bloodGroup;

    // For organ requests
    private String organNeeded;

    private String patientName;

    private String hospitalName;

    private String city;

    private Double latitude;

    private Double longitude;

    private String contactPhone;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime fulfilledAt;

    public enum RequestType {
        BLOOD, ORGAN
    }

    public enum Status {
        OPEN, FULFILLED, CANCELLED
    }
}
