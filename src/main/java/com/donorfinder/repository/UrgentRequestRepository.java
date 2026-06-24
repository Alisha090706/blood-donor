package com.donorfinder.repository;

import com.donorfinder.model.UrgentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrgentRequestRepository extends JpaRepository<UrgentRequest, Long> {
    List<UrgentRequest> findByRequestedByIdOrderByCreatedAtDesc(Long userId);
    List<UrgentRequest> findByStatusOrderByCreatedAtDesc(UrgentRequest.Status status);
}
