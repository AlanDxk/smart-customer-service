package org.example.smart.user.repository;

import org.example.smart.user.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * 用户仓库接口（响应式）
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);
}