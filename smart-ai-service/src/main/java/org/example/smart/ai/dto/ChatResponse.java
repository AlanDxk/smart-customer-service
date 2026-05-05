package org.example.smart.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天响应")
public class ChatResponse {

    @Schema(description = "会话ID", example = "1")
    private Long conversationId;

    @Schema(description = "AI回复内容", example = "您好！请问有什么可以帮助您的？")
    private String reply;
}