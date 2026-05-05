package org.example.smart.ai.service.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smart.ai.dto.ChatResponse;
import org.example.smart.ai.entity.Conversation;
import org.example.smart.ai.entity.Message;
import org.example.smart.ai.repository.ConversationRepository;
import org.example.smart.ai.repository.MessageRepository;
import org.example.smart.ai.service.ConversationService;
import org.example.smart.ai.vo.ConversationVO;
import org.example.smart.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {


    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * 发送消息并获取 AI 回复
     *
     * @param userId         用户ID
     * @param conversationId 会话ID（为 null 则创建新会话）
     * @param userMessage    用户消息内容
     * @return AI 回复及会话ID
     */
    public Mono<ChatResponse> chat(Long userId, Long conversationId, String userMessage) {
//        //把请求存入数据库
//        Message message = Message.builder()
//                .userId(userId)
//                .conversationId(conversationId)
//                .content(userMessage)
//                .role("user")
//                .build();
//        messageRepository.save(message);
//
//        String result = model.chat(userMessage);
//        //把回复存入数据库
//        Message aiMessage = Message.builder()
//                .userId(userId)
//                .conversationId(conversationId)
//                .content(result)
//                .role("assistant")
//                .build();
//        messageRepository.save(aiMessage);
//        return Mono.just(ChatResponse.builder()
//                .conversationId(aiMessage.getConversationId())
//                .reply(result)
//                .build());
        return Mono.empty();
    }

    @Override
    public Flux<String> chat(String userId, String userMessage) {
        return null;
    }

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表，按创建时间倒序
     */
    public Flux<ConversationVO> listConversations(Long userId) {
        // 1. 从数据库查询Conversation实体
        Flux<Conversation> conversationFlux = conversationRepository.findByUserIdOrderByCreateTimeDesc(userId, 0);

        // 2. 转换为ConversationVO并添加statusName
        Flux<ConversationVO> voFlux = conversationFlux.map(conversation -> ConversationVO.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .status(conversation.getStatus())
                .statusName(ConversationVO.getStatusName(conversation.getStatus()))
                .startTime(conversation.getStartTime())
                .updateTime(conversation.getUpdateTime())
                .createTime(conversation.getCreateTime())
                .build());

        // 3. 收集为 List
        Mono<List<ConversationVO>> listMono = voFlux.collectList();

        // 4. 包装为统一响应格式
        Mono<ApiResponse<List<ConversationVO>>> result = listMono.map(ApiResponse::success);

        return voFlux;
    }

    /**
     * 获取会话的消息历史
     *
     * @param conversationId 会话ID
     * @param userId         用户ID（用于权限校验）
     * @return 消息列表，按创建时间正序
     */
    public Flux<Message> getMessages(Long conversationId, Long userId) {
        // 1. 从数据库查询
        Flux<Message> messageFlux = messageRepository.findByConversationIdAndUserId(conversationId, userId, 0);

        // 2. 按创建时间正序
        messageFlux = messageFlux.sort(Comparator.comparing(Message::getCreateTime));

        // 3. 返回结果
        return messageFlux;
    }
}