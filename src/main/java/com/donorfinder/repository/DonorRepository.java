package com.donorfinder.repository;

import com.donorfinder.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByUserId(Long userId);

    // Haversine formula: finds donors within `radiusKm` km from given lat/lon
    @Query(value = """
        SELECT d.* FROM donors d
        WHERE d.available_for_blood = true
          AND d.blood_group = :bloodGroup
          AND (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(d.latitude)) *
              cos(radians(d.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(d.latitude))
            )
          ) <= :radiusKm
        ORDER BY (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(d.latitude)) *
              cos(radians(d.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(d.latitude))
            )
        ) ASC
        """, nativeQuery = true)
    List<Donor> findNearbyBloodDonors(
            @Param("bloodGroup") String bloodGroup,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusKm") Double radiusKm
    );

    @Query(value = """
        SELECT d.* FROM donors d
        WHERE d.available_for_organ = true
          AND d.organs_willing_to_donate LIKE CONCAT('%', :organ, '%')
          AND (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(d.latitude)) *
              cos(radians(d.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(d.latitude))
            )
          ) <= :radiusKm
        ORDER BY (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(d.latitude)) *
              cos(radians(d.longitude) - radians(:lon)) +
              sin(radians(:lat)) * sin(radians(d.latitude))
            )
        ) ASC
        """, nativeQuery = true)
    List<Donor> findNearbyOrganDonors(
            @Param("organ") String organ,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusKm") Double radiusKm
    );
}
