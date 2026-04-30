package org.example.smart.ticket.dto;

import lombok.Data;

@Data
public class TicketRequest {
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
}