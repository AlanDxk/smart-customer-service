package org.example.smart.user.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("users")
public class User {

    @Id
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String realName;

    private String avatarUrl;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastLoginTime;

    private Integer loginCount;

    private Integer isDeleted;
}