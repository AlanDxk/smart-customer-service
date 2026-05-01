package org.example.smart.ticket.service.Impl;

import lombok.RequiredArgsConstructor;
import org.example.smart.ticket.dto.TicketRequest;
import org.example.smart.ticket.dto.TicketResponse;
import org.example.smart.ticket.entity.Ticket;
import org.example.smart.ticket.repository.TicketRepository;
import org.example.smart.ticket.service.TicketService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public Mono<TicketResponse> create(TicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setContent(request.getContent());
        ticket.setTypeId(request.getTypeId());
        ticket.setPriority(request.getPriority());
        ticket.setAssigneeId(request.getAssigneeId());
        ticket.setHandlerId(request.getHandlerId());
        ticket.setDepartmentId(request.getDepartmentId());
        ticket.setSource(request.getSource());
        ticket.setUserId(request.getUserId());
        ticket.setStatusId(Long.valueOf(1)); // 默认状态：待处理
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticket.setIsDeleted(0);

        return ticketRepository.save(ticket)
                .map(this::toResponse);
    }

    @Override
    public Mono<TicketResponse> findById(Long id, Long userId) {
        return ticketRepository.findByIdAndUserId(id, userId)
                .map(this::toResponse);
    }

    @Override
    public Flux<TicketResponse> findByUserId(Long userId) {
        return ticketRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    @Override
    public Mono<TicketResponse> update(Long id, Long userId, TicketRequest request) {
        return ticketRepository.findByIdAndUserId(id, userId)
                .flatMap(existingTicket -> {
                    existingTicket.setTitle(request.getTitle());
                    existingTicket.setContent(request.getContent());
                    existingTicket.setTypeId(request.getTypeId());
                    existingTicket.setStatusId(request.getStatusId());
                    existingTicket.setPriority(request.getPriority());
                    existingTicket.setAssigneeId(request.getAssigneeId());
                    existingTicket.setHandlerId(request.getHandlerId());
                    existingTicket.setDepartmentId(request.getDepartmentId());
                    existingTicket.setSource(request.getSource());
                    existingTicket.setUpdateTime(LocalDateTime.now());
                    
                    return ticketRepository.save(existingTicket)
                            .map(this::toResponse);
                });
    }

    @Override
    public Mono<Void> delete(Long id, Long userId) {
        return ticketRepository.findByIdAndUserId(id, userId)
                .flatMap(ticket -> {
                    ticket.setIsDeleted(1);
                    ticket.setUpdateTime(LocalDateTime.now());
                    return ticketRepository.save(ticket).then();
                });
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .content(ticket.getContent())
                .typeId(ticket.getTypeId())
                .statusId(ticket.getStatusId())
                .priority(ticket.getPriority())
                .assigneeId(ticket.getAssigneeId())
                .handlerId(ticket.getHandlerId())
                .departmentId(ticket.getDepartmentId())
                .source(ticket.getSource())
                .userId(ticket.getUserId())
                .createTime(ticket.getCreateTime())
                .updateTime(ticket.getUpdateTime())
                .closeTime(ticket.getCloseTime())
                .build();
    }
}