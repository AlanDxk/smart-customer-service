package org.example.smart.ticket.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String title;
    private String content;
    private Long typeId;
    private Long statusId;
    private Integer priority;
    private Long assigneeId;
    private Long handlerId;
    private Long departmentId;
    private String source;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime closeTime;
}