package com.donorfinder.dto;

import com.donorfinder.model.Donor;
import com.donorfinder.model.UrgentRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class RequestDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Request type is required (BLOOD or ORGAN)")
        private UrgentRequest.RequestType requestType;

        // Required for BLOOD
        private Donor.BloodGroup bloodGroup;

        // Required for ORGAN
        private String organNeeded;

        @NotBlank(message = "Patient name is required")
        private String patientName;

        @NotBlank(message = "Hospital name is required")
        private String hospitalName;

        private String city;

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        @NotBlank(message = "Contact phone is required")
        private String contactPhone;

        private String description;
    }

    @Data
    public static class RequestResponse {
        private Long id;
        private String requestType;
        private String bloodGroup;
        private String organNeeded;
        private String patientName;
        private String hospitalName;
        private String city;
        private String contactPhone;
        private String description;
        private String status;
        private String requestedByName;
        private LocalDateTime createdAt;

        public static RequestResponse from(UrgentRequest r) {
            RequestResponse res = new RequestResponse();
            res.id = r.getId();
            res.requestType = r.getRequestType().name();
            res.bloodGroup = r.getBloodGroup() != null ? r.getBloodGroup().getLabel() : null;
            res.organNeeded = r.getOrganNeeded();
            res.patientName = r.getPatientName();
            res.hospitalName = r.getHospitalName();
            res.city = r.getCity();
            res.contactPhone = r.getContactPhone();
            res.description = r.getDescription();
            res.status = r.getStatus().name();
            res.requestedByName = r.getRequestedBy().getName();
            res.createdAt = r.getCreatedAt();
            return res;
        }
    }
}
