package org.example.smart.ticket.repository;

import org.example.smart.ticket.entity.Ticket;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TicketRepository extends R2dbcRepository<Ticket, Long> {
    
    /**
     * 根据用户ID查询工单
     * @param userId 用户ID
     * @return 工单列表
     */
    Flux<Ticket> findByUserId(Long userId);
    
    /**
     * 根据ID和用户ID查询工单
     * @param id 工单ID
     * @param userId 用户ID
     * @return 工单
     */
    Mono<Ticket> findByIdAndUserId(Long id, Long userId);
}