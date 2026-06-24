package com.donorfinder.service;

import com.donorfinder.dto.RequestDto;
import com.donorfinder.exception.BadRequestException;
import com.donorfinder.exception.ResourceNotFoundException;
import com.donorfinder.model.Donor;
import com.donorfinder.model.UrgentRequest;
import com.donorfinder.model.User;
import com.donorfinder.repository.DonorRepository;
import com.donorfinder.repository.UrgentRequestRepository;
import com.donorfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrgentRequestService {

    private final UrgentRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final EmailService emailService;

    @Value("${app.donor.search-radius-km:50}")
    private Double defaultRadiusKm;

    public RequestDto.RequestResponse createRequest(String email, RequestDto.CreateRequest req) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate type-specific fields
        if (req.getRequestType() == UrgentRequest.RequestType.BLOOD && req.getBloodGroup() == null) {
            throw new BadRequestException("Blood group is required for a blood request");
        }
        if (req.getRequestType() == UrgentRequest.RequestType.ORGAN &&
                (req.getOrganNeeded() == null || req.getOrganNeeded().isBlank())) {
            throw new BadRequestException("Organ name is required for an organ request");
        }

        UrgentRequest urgentRequest = UrgentRequest.builder()
                .requestedBy(requester)
                .requestType(req.getRequestType())
                .bloodGroup(req.getBloodGroup())
                .organNeeded(req.getOrganNeeded())
                .patientName(req.getPatientName())
                .hospitalName(req.getHospitalName())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .contactPhone(req.getContactPhone())
                .description(req.getDescription())
                .status(UrgentRequest.Status.OPEN)
                .build();

        requestRepository.save(urgentRequest);

        // Find nearby donors and notify them asynchronously
        notifyNearbyDonors(urgentRequest);

        // Send confirmation to requester
        emailService.sendRequestConfirmation(urgentRequest);

        return RequestDto.RequestResponse.from(urgentRequest);
    }

    public List<RequestDto.RequestResponse> getOpenRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(UrgentRequest.Status.OPEN)
                .stream()
                .map(RequestDto.RequestResponse::from)
                .toList();
    }

    public List<RequestDto.RequestResponse> getMyRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return requestRepository.findByRequestedByIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(RequestDto.RequestResponse::from)
                .toList();
    }

    public RequestDto.RequestResponse markFulfilled(Long requestId, String email) {
        UrgentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getRequestedBy().getEmail().equals(email)) {
            throw new BadRequestException("You can only update your own requests");
        }

        request.setStatus(UrgentRequest.Status.FULFILLED);
        request.setFulfilledAt(java.time.LocalDateTime.now());
        requestRepository.save(request);

        return RequestDto.RequestResponse.from(request);
    }

    public RequestDto.RequestResponse cancelRequest(Long requestId, String email) {
        UrgentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getRequestedBy().getEmail().equals(email)) {
            throw new BadRequestException("You can only cancel your own requests");
        }

        request.setStatus(UrgentRequest.Status.CANCELLED);
        requestRepository.save(request);

        return RequestDto.RequestResponse.from(request);
    }

    private void notifyNearbyDonors(UrgentRequest request) {
        List<Donor> donors;

        if (request.getRequestType() == UrgentRequest.RequestType.BLOOD) {
            donors = donorRepository.findNearbyBloodDonors(
                    request.getBloodGroup().name(),
                    request.getLatitude(),
                    request.getLongitude(),
                    defaultRadiusKm
            );
        } else {
            donors = donorRepository.findNearbyOrganDonors(
                    request.getOrganNeeded().toUpperCase(),
                    request.getLatitude(),
                    request.getLongitude(),
                    defaultRadiusKm
            );
        }

        log.info("Found {} nearby donors for request #{}", donors.size(), request.getId());
        emailService.notifyDonors(donors, request);
    }
}
