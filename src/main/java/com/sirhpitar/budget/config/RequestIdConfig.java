package com.sirhpitar.budget.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import java.util.UUID;

@Configuration
public class RequestIdConfig {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_ATTR = "requestId";

    @Bean
    public WebFilter requestIdWebFilter() {
        return (exchange, chain) -> {
            String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }

            exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);

            exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

            return chain.filter(exchange);
        };
    }

    public static String getRequestId(ServerWebExchange exchange) {
        Object v = exchange.getAttribute(REQUEST_ID_ATTR);
        return v == null ? null : v.toString();
    }
}