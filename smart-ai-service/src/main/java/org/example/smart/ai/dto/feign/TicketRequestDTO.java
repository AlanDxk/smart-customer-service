package org.example.smart.ai.dto.feign;

import lombok.Data;

@Data
public class TicketRequestDTO {
    private String title;
    private String content;
    private Long typeId;
    private Integer priority;
    private String source;
    private Long userId;
}
