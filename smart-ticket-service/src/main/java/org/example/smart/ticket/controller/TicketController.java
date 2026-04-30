package org.example.smart.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.smart.ticket.dto.TicketRequest;
import org.example.smart.ticket.dto.TicketResponse;
import org.example.smart.ticket.service.TicketService;
import org.example.smart.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "工单管理", description = "工单的增删改查接口")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "创建工单", description = "创建新的工单")
    public Mono<ApiResponse<TicketResponse>> create(
            @Parameter(description = "工单请求参数", required = true)
            @RequestBody TicketRequest request) {
        return ticketService.create(request)
                .map(ApiResponse::success);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工单", description = "根据工单ID和用户ID查询工单")
    public Mono<ApiResponse<TicketResponse>> findById(
            @Parameter(description = "工单ID", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "用户ID", required = true)
            @RequestParam("userId") Long userId) {
        return ticketService.findById(id, userId)
                .map(ApiResponse::success);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询工单", description = "查询指定用户的所有工单")
    public Flux<TicketResponse> findByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        return ticketService.findByUserId(userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工单", description = "更新指定工单的信息")
    public Mono<ApiResponse<TicketResponse>> update(
            @Parameter(description = "工单ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "工单请求参数", required = true)
            @RequestBody TicketRequest request) {
        return ticketService.update(id, userId, request)
                .map(ApiResponse::success);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工单", description = "软删除指定工单")
    public Mono<ApiResponse<Void>> delete(
            @Parameter(description = "工单ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {
        return ticketService.delete(id, userId)
                .thenReturn(ApiResponse.success(null));
    }
}