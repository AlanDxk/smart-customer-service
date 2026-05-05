package org.example.smart.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息返回对象")
public class MessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "所属会话ID")
    private Long conversationId;

    @Schema(description = "发送者用户ID（AI消息时为空）")
    private Long userId;

    @Schema(description = "消息角色：user=用户, assistant=AI助手, system=系统")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型：text=文本, image=图片, file=文件, tool_call=工具调用")
    private String messageType;

    @Schema(description = "附件文件URL")
    private String fileUrl;

    @Schema(description = "消息发送时间")
    private LocalDateTime createTime;
}