package org.example.smart.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationVO {

    private Long id;

    private String title;

    private Integer status;

    private String statusName;

    private LocalDateTime startTime;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }
}