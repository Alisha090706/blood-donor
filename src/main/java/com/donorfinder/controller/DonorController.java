package com.donorfinder.controller;

import com.donorfinder.dto.ApiResponse;
import com.donorfinder.dto.DonorDto;
import com.donorfinder.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDto.ProfileResponse>> saveProfile(
            @Valid @RequestBody DonorDto.ProfileRequest req, Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Profile saved", donorService.createOrUpdateProfile(principal.getName(), req)));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDto.ProfileResponse>> getProfile(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched", donorService.getMyProfile(principal.getName())));
    }

    @GetMapping("/eligibility")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDto.EligibilityCheckResponse>> checkEligibility(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Eligibility checked", donorService.checkEligibility(principal.getName())));
    }

    @PostMapping("/search/blood")
    public ResponseEntity<ApiResponse<List<DonorDto.ProfileResponse>>> searchBloodDonors(
            @RequestBody DonorDto.SearchRequest req) {
        List<DonorDto.ProfileResponse> donors = donorService.searchBloodDonors(req);
        return ResponseEntity.ok(ApiResponse.ok(donors.size() + " blood donor(s) found", donors));
    }

    @PostMapping("/search/organ")
    public ResponseEntity<ApiResponse<List<DonorDto.ProfileResponse>>> searchOrganDonors(
            @RequestBody DonorDto.SearchRequest req) {
        List<DonorDto.ProfileResponse> donors = donorService.searchOrganDonors(req);
        return ResponseEntity.ok(ApiResponse.ok(donors.size() + " organ donor(s) found", donors));
    }

    @PatchMapping("/availability/blood")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDto.ProfileResponse>> toggleBloodAvailability(
            @RequestParam boolean available, Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Blood availability updated",
                donorService.toggleAvailability(principal.getName(), true, available)));
    }

    @PatchMapping("/availability/organ")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<DonorDto.ProfileResponse>> toggleOrganAvailability(
            @RequestParam boolean available, Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Organ availability updated",
                donorService.toggleAvailability(principal.getName(), false, available)));
    }
}
