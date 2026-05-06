package org.example.smart.ai.client;

import lombok.RequiredArgsConstructor;
import org.example.smart.ai.dto.feign.TicketRequestDTO;
import org.example.smart.ai.dto.feign.TicketResponseDTO;
import org.example.smart.common.response.ApiResponse;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TicketClient {

    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    private WebClient getWebClient() {
        return WebClient.builder()
                .filter(lbFunction)
                .baseUrl("http://smart-ticket-service")
                .build();
    }

    public Mono<ApiResponse<TicketResponseDTO>> createTicket(TicketRequestDTO request) {
        return getWebClient()
                .post()
                .uri("/api/tickets")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<TicketResponseDTO>>() {});
    }
}
