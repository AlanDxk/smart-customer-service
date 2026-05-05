package org.example.smart.ai.controller;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smart.ai.dto.ChatRequest;
import org.example.smart.ai.dto.ChatResponse;
import org.example.smart.ai.entity.Message;
import org.example.smart.ai.service.ConsultantService;
import org.example.smart.ai.service.ConversationService;
import org.example.smart.ai.vo.ConversationVO;
import org.example.smart.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@Tag(name = "聊天接口", description = "提供AI多轮对话功能")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationService conversationService;

    @Autowired
    private ConsultantService consultantService;

    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    @Operation(summary = "发送消息", description = "向AI发送消息并获取回复，支持多轮对话上下文",
    parameters = {
        @Parameter(name = "memoryId", description = "记忆ID（如用户ID）", required = true),
        @Parameter(name = "message", description = "用户输入消息", required = true)
    })
    public Flux<String> chat(
            @RequestParam("memoryId") String memoryId,
            @RequestParam("message") String message){
        Flux<String> result = consultantService.chat(memoryId,message);
        return result;
//                .doOnNext(token -> {
//                    // 每输出一个字/一段，就打一行日志
//                    log.info("流式输出内容：{}", token);
//                })
//                .doOnComplete(() -> {
//                    log.info("流式对话完成");
//                })
//                .doOnError(e -> {
//                    log.error("流式输出异常", e);
//                });
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有对话会话")
    public Flux<ConversationVO> listConversations(
            @Parameter(description = "当前用户ID") @RequestHeader("userId") Long userId) {
        return conversationService.listConversations(userId);
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息记录")
    public Flux<Message> getMessages(
            @Parameter(description = "当前用户ID") @RequestHeader("userId") Long userId,
            @Parameter(description = "会话ID") @PathVariable("id") Long conversationId) {
        return conversationService.getMessages(conversationId, userId);
    }
}