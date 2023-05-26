package liar.waitservice.common.config;

import liar.waitservice.wait.controller.handler.CustomWebSocketHandlerDecorator;
import liar.waitservice.wait.controller.interceptor.WebsocketSecurityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebsocketSecurityInterceptor websocketSecurityInterceptor;

    /**
     * Configure options related to the processing of messages received from and
     * sent to WebSocket clients.
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(this::customWebSocketHandlerDecorator);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wait-service/wait-websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/wait-service/waitroom/sub", "/queue");
        registry.setApplicationDestinationPrefixes("/wait-service");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(websocketSecurityInterceptor);
    }

    @Bean
    public WebSocketHandlerDecorator customWebSocketHandlerDecorator(@Qualifier("subProtocolWebSocketHandler") WebSocketHandler webSocketHandler) {
        return new CustomWebSocketHandlerDecorator(webSocketHandler);
    }
}
