package org.example.smart.ticket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
@Builder

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tickets")
public class Ticket {

    @Id
    private Long id;

    private String title;

    private String content;

    @Column("user_id")
    private Long userId;

    @Column("type_id")
    private Long typeId;

    @Column("status_id")
    private Long statusId;

    private Integer priority;

    @Column("assignee_id")
    private Long assigneeId;

    @Column("handler_id")
    private Long handlerId;

    @Column("department_id")
    private Long departmentId;

    private String source;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;

    @Column("close_time")
    private LocalDateTime closeTime;

    @Column("is_deleted")
    private Integer isDeleted;
}