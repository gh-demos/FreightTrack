package com.freighttrack.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String recipient;
    private String type;
    private String message;
    private String subject;
    private Long shipmentId;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String deliveryChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
