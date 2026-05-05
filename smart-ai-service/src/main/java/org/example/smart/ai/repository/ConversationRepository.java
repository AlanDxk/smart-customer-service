package org.example.smart.ai.repository;

import org.example.smart.ai.entity.Conversation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ConversationRepository extends R2dbcRepository<Conversation, Long> {

    @Query("SELECT * FROM conversations WHERE user_id = :userId AND is_deleted = :isDeleted ORDER BY create_time DESC")
    Flux<Conversation> findByUserIdOrderByCreateTimeDesc(Long userId, Integer isDeleted);

    Flux<Conversation> findByUserIdAndIsDeleted(Long userId, Integer isDeleted);

    Mono<Conversation> findByIdAndUserIdAndIsDeleted(Long conversationId, Long userId, Integer isDeleted);
}