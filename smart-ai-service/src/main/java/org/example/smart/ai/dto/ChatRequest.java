package org.example.smart.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天请求")
public class ChatRequest {

    @Schema(description = "消息内容", example = "你好，我想咨询一个问题")
    private String message;

    @Schema(description = "会话ID（可选，不传则创建新会话）", example = "1")
    private Long conversationId;
}