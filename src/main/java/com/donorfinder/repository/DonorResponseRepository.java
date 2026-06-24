package com.donorfinder.repository;

import com.donorfinder.model.DonorResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {
    List<DonorResponse> findByRequestId(Long requestId);
    List<DonorResponse> findByDonorId(Long donorId);
    Optional<DonorResponse> findByRequestIdAndDonorId(Long requestId, Long donorId);
}
