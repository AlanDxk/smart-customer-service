package org.example.smart.common.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign 客户端接口
 * 这是一个空接口，后续可以根据需要添加具体的 Feign 方法
 */
@FeignClient(name = "client-service", url = "http://localhost:8080") // 可以根据实际服务名和URL配置
public interface ClientFeignService {

    // TODO: 后续在此添加具体的 Feign 方法
    // 例如：
    // @GetMapping("/api/clients/{id}")
    // ApiResponse<Client> getClientById(@PathVariable("id") Long id);

    // @PostMapping("/api/clients")
    // ApiResponse<Client> createClient(@RequestBody Client client);

    // @PutMapping("/api/clients/{id}")
    // ApiResponse<Client> updateClient(@PathVariable("id") Long id, @RequestBody Client client);

    // @DeleteMapping("/api/clients/{id}")
    // ApiResponse<Void> deleteClient(@PathVariable("id") Long id);
}