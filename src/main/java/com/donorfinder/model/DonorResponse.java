package com.donorfinder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "donor_responses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private UrgentRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus status = ResponseStatus.PENDING;

    private String message;

    @CreationTimestamp
    private LocalDateTime respondedAt;

    public enum ResponseStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
