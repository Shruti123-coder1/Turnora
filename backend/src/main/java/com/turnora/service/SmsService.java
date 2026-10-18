package com.turnora.service;

import com.turnora.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Sends SMS in a background thread. A failed SMS never stops the queue.
// SMS is OFF until sms.enabled=true and a provider url and api key are set.
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final NotificationRepository notificationRepository;

    @Value("${sms.enabled:false}")
    private boolean enabled;

    @Value("${sms.provider.url:}")
    private String url;

    @Value("${sms.provider.api-key:}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    @Async("smsExecutor")
    public void send(Long notificationId, String phone, String message) {
        String status;

        if (!enabled || url == null || url.isBlank() || url.startsWith("PROVIDER")) {
            log.info("SMS is switched off. Not sent to {}: {}", mask(phone), message);
            status = "SKIPPED";
        } else {
            try {
                // Request in Fast2SMS style. Change this part when you choose another SMS provider.
                String form = "route=q"
                        + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                        + "&numbers=" + phone
                        + "&flash=0";

                restClient.post()
                        .uri(url)
                        .header("authorization", apiKey)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .toBodilessEntity();
                status = "SENT";
            } catch (Exception e) {
                log.warn("SMS to {} failed: {}", mask(phone), e.getMessage());
                status = "FAILED";
            }
        }

        saveStatus(notificationId, status);
    }

    private void saveStatus(Long notificationId, String status) {
        try {
            notificationRepository.findById(notificationId).ifPresent(n -> {
                n.setSmsStatus(status);
                notificationRepository.save(n);
            });
        } catch (Exception e) {
            log.warn("Could not save SMS status: {}", e.getMessage());
        }
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "******" + phone.substring(phone.length() - 4);
    }
}
