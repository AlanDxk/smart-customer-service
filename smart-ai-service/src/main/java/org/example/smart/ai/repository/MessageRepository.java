package org.example.smart.ai.repository;

import org.example.smart.ai.entity.Message;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends R2dbcRepository<Message, Long> {

    Flux<Message> findByConversationIdAndUserId(Long conversationId, Long userId, Integer isDeleted);
}