package org.example.smart.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.example.smart.ai.client.TicketClient;
import org.example.smart.ai.dto.feign.TicketRequestDTO;
import org.example.smart.ai.dto.feign.TicketResponseDTO;
import org.example.smart.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ticketTool")
public class TicketTool {

    @Autowired
    private TicketClient ticketClient;

    @Tool("创建工单。当用户需要创建工单、提交问题、报修、投诉时调用此工具。需要提供工单标题和内容描述，类型ID和优先级可选")
    public String createTicket(
            @P("工单标题") String title,
            @P("工单内容描述") String content,
            @P(value = "工单类型ID，默认为1", required = false) Long typeId,
            @P(value = "优先级：1-低 2-中 3-高，默认为2", required = false) Integer priority,
            @ToolMemoryId String memoryId) {

        String userId = memoryId;
        log.info("AI Agent 创建工单: userId={}, title={}, content={}, typeId={}, priority={}",
                userId, title, content, typeId, priority);

        TicketRequestDTO request = new TicketRequestDTO();
        request.setTitle(title);
        request.setContent(content != null ? content : title);
        request.setTypeId(typeId != null ? typeId : 1L);
        request.setPriority(priority != null ? priority : 2);
        request.setUserId(Long.valueOf(userId));
        request.setSource("AI");

        try {
            TicketResponseDTO response = ticketClient.createTicket(request)
                    .map(ApiResponse::getData)
                    .block();

            log.info("工单创建成功: ticketId={}", response.getId());
            return "工单创建成功！工单ID：" + response.getId()
                    + "，标题：" + response.getTitle()
                    + "，状态ID：" + response.getStatusId();
        } catch (Exception e) {
            log.error("创建工单失败", e);
            return "创建工单失败：" + e.getMessage();
        }
    }
}
