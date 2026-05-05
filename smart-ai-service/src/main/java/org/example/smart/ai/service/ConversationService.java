package org.example.smart.ai.service;


import org.example.smart.ai.dto.ChatResponse;
import org.example.smart.ai.entity.Message;
import org.example.smart.ai.vo.ConversationVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ConversationService {


    /**
     * 发送消息并获取 AI 回复
     *
     * @param userId         用户ID
     * @param conversationId 会话ID（为 null 则创建新会话）
     * @param userMessage    用户消息内容
     * @return AI 回复及会话ID
     */
    Flux<String> chat(String userId, String userMessage);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表，按创建时间倒序
     */
    Flux<ConversationVO> listConversations(Long userId);

    /**
     * 获取会话的消息历史
     *
     * @param conversationId 会话ID
     * @param userId         用户ID（用于权限校验）
     * @return 消息列表，按创建时间正序
     */
    Flux<Message> getMessages(Long conversationId, Long userId);
}