package org.example.smart.ticket.service;

import org.example.smart.ticket.dto.TicketRequest;
import org.example.smart.ticket.dto.TicketResponse;
import org.example.smart.ticket.vo.TicketVO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TicketService {
    
    /**
     * 创建工单
     * @param request 工单请求
     * @return 工单响应
     */
    Mono<TicketResponse> create(TicketRequest request);
    
    /**
     * 根据ID查询工单
     * @param id 工单ID
     * @param userId 用户ID
     * @return 工单响应
     */
    Mono<TicketResponse> findById(Long id, Long userId);
    
    /**
     * 根据用户ID查询所有工单
     * @param userId 用户ID
     * @return 工单列表
     */
    Flux<TicketResponse> findByUserId(Long userId);
    
    /**
     * 更新工单
     * @param id 工单ID
     * @param userId 用户ID
     * @param request 工单请求
     * @return 更新后的工单
     */
    Mono<TicketResponse> update(Long id, Long userId, TicketRequest request);
    
    /**
     * 删除工单
     * @param id 工单ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Mono<Void> delete(Long id, Long userId);
}