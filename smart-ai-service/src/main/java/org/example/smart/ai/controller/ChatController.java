package org.example.smart.ai.controller;


import dev.langchain4j.model.openai.OpenAiChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.smart.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "聊天接口", description = "提供AI对话功能")
public class ChatController {
    @Resource
    private OpenAiChatModel model;
    @GetMapping("/chat")
    @Operation(summary = "测试聊天", description = "测试AI对话功能")
    public ApiResponse<String> chat(@RequestParam("message")  String message){
        String result = model.chat(message);
        return ApiResponse.success(result);
    }
}
