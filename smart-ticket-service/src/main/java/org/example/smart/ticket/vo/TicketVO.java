package org.example.smart.ticket.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer priority;
    private String priorityName;
    private Integer status;
    private String statusName;
    private String contactInfo;
    private Long userId;
    private String username;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime resolveTime;

    public static String getPriorityName(Integer priority) {
        if (priority == null) return "未知";
        return switch (priority) {
            case 0 -> "低";
            case 1 -> "中";
            case 2 -> "高";
            case 3 -> "紧急";
            default -> "未知";
        };
    }

    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "处理中";
            case 2 -> "已完成";
            case 3 -> "已关闭";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}