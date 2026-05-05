package org.example.smart.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("messages")
public class Message {

    @Id
    private Long id;

    @Column("conversation_id")
    private Long conversationId;

    @Column("user_id")
    private Long userId;

    private String role;

    private String content;

    @Column("message_type")
    private String messageType;

    @Column("file_url")
    private String fileUrl;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("is_deleted")
    private Integer isDeleted;
}