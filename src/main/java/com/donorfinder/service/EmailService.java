package com.donorfinder.service;

import com.donorfinder.model.Donor;
import com.donorfinder.model.UrgentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void notifyDonors(List<Donor> donors, UrgentRequest request) {
        for (Donor donor : donors) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(donor.getUser().getEmail());
                message.setSubject("🚨 Urgent " + request.getRequestType().name() + " Request Near You");
                message.setText(buildEmailBody(donor, request));
                mailSender.send(message);
                log.info("Email sent to donor: {}", donor.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", donor.getUser().getEmail(), e.getMessage());
            }
        }
    }

    @Async
    public void sendRequestConfirmation(UrgentRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRequestedBy().getEmail());
            message.setSubject("✅ Your Request Has Been Posted");
            message.setText("""
                    Dear %s,
                    
                    Your urgent %s request for patient "%s" has been posted successfully.
                    
                    We are notifying nearby donors in your area. You will be contacted shortly.
                    
                    Request ID: %d
                    Hospital: %s
                    Contact: %s
                    
                    Stay strong. Help is on the way.
                    
                    — Donor Finder Team
                    """.formatted(
                    request.getRequestedBy().getName(),
                    request.getRequestType().name(),
                    request.getPatientName(),
                    request.getId(),
                    request.getHospitalName(),
                    request.getContactPhone()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send confirmation email: {}", e.getMessage());
        }
    }

    private String buildEmailBody(Donor donor, UrgentRequest request) {
        String typeInfo = request.getRequestType() == UrgentRequest.RequestType.BLOOD
                ? "Blood Group: " + request.getBloodGroup().getLabel()
                : "Organ Needed: " + request.getOrganNeeded();

        return """
                Dear %s,
                
                Someone near you urgently needs your help.
                
                Patient: %s
                %s
                Hospital: %s
                City: %s
                Contact: %s
                
                %s
                
                Please respond as soon as possible if you are able to help.
                Log in to the app to confirm your response.
                
                You are saving a life. Thank you.
                
                — Donor Finder Team
                """.formatted(
                donor.getUser().getName(),
                request.getPatientName(),
                typeInfo,
                request.getHospitalName(),
                request.getCity(),
                request.getContactPhone(),
                request.getDescription() != null ? request.getDescription() : ""
        );
    }
}
