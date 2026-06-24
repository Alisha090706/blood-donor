package com.donorfinder.controller;

import com.donorfinder.dto.ApiResponse;
import com.donorfinder.dto.RequestDto;
import com.donorfinder.service.UrgentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class UrgentRequestController {

    private final UrgentRequestService requestService;

    // Post a new urgent request (blood or organ)
    @PostMapping
    public ResponseEntity<ApiResponse<RequestDto.RequestResponse>> createRequest(
            @Valid @RequestBody RequestDto.CreateRequest req,
            Principal principal) {
        RequestDto.RequestResponse response = requestService.createRequest(principal.getName(), req);
        return ResponseEntity.ok(ApiResponse.ok("Request posted. Nearby donors are being notified.", response));
    }

    // Get all open requests — public endpoint
    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<RequestDto.RequestResponse>>> getOpenRequests() {
        List<RequestDto.RequestResponse> requests = requestService.getOpenRequests();
        return ResponseEntity.ok(ApiResponse.ok(requests.size() + " open request(s)", requests));
    }

    // Get my posted requests
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<RequestDto.RequestResponse>>> getMyRequests(Principal principal) {
        List<RequestDto.RequestResponse> requests = requestService.getMyRequests(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Your requests", requests));
    }

    // Mark a request as fulfilled
    @PatchMapping("/{id}/fulfill")
    public ResponseEntity<ApiResponse<RequestDto.RequestResponse>> markFulfilled(
            @PathVariable Long id, Principal principal) {
        RequestDto.RequestResponse response = requestService.markFulfilled(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Request marked as fulfilled 🎉", response));
    }

    // Cancel a request
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RequestDto.RequestResponse>> cancelRequest(
            @PathVariable Long id, Principal principal) {
        RequestDto.RequestResponse response = requestService.cancelRequest(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Request cancelled", response));
    }
}
