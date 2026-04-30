package org.example.smart.ticket.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketQueryRequest {
    private String title;
    private String type;
    private Integer priority;
    private Integer status;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 10;
}