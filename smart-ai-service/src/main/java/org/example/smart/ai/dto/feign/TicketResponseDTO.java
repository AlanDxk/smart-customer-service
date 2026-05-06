package org.example.smart.ai.dto.feign;

import lombok.Data;

@Data
public class TicketResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Long typeId;
    private Long statusId;
    private Integer priority;
    private Long userId;
    private String source;
}
