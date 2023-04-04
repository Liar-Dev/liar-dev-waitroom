package liar.waitservice.wait.controller.interceptor;

import liar.waitservice.common.auth.token.tokenprovider.TokenProviderPolicy;
import liar.waitservice.exception.exception.WebsocketSecurityException;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebsocketSecurityInterceptor implements ChannelInterceptor {

    private final WaitRoomFacadeService waitRoomFacadeService;
    private final AntPathMatcher antPathMatcher;
    private final TokenProviderPolicy tokenProviderPolicy;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            isValidateWaitRoomIdAndJoinMember(headerAccessor);
        }
        return message;
    }

    /**
     * 참여 조건: accessToken, refreshToken, waitRoomId, userId 존재
     * 토큰 유효성, 방 유효성, 유저 유효성 통과
     * 현재 방에 참여하지 않은 유저만 재 join 가능
     */
    private void isValidateWaitRoomIdAndJoinMember(StompHeaderAccessor headerAccessor) {
        String accessToken = headerAccessor.getFirstNativeHeader("Authorization");
        String refreshToken = headerAccessor.getFirstNativeHeader("RefreshToken");
        String waitRoomId = headerAccessor.getFirstNativeHeader("WaitRoomId");
        String userId = headerAccessor.getFirstNativeHeader("UserId");

        if (accessToken == null || refreshToken == null || waitRoomId == null || userId == null)
            throw new WebsocketSecurityException();

        validateUserAccessor(validateTokenAccessor(accessToken, refreshToken), userId);

        String destination = headerAccessor.getDestination();
        if (destination == null) throw new WebsocketSecurityException();

        if (isApplyUri(destination)) {
            waitRoomFacadeService.isJoinedMemberThenThrow(waitRoomId, userId); // 이미 조인된 유저는 BadRequestException()
        }
    }

    private String validateTokenAccessor(String accessToken, String refreshToken) {
        try {
            String userIdFromAccess = tokenProviderPolicy.getUserIdFromToken(tokenProviderPolicy.removeType(accessToken));
            String userIdFromRefresh = tokenProviderPolicy.getUserIdFromToken(refreshToken);

            if (!userIdFromAccess.equals(userIdFromRefresh)) throw new WebsocketSecurityException();

            return userIdFromAccess;
        } catch (Exception e) {
            throw new WebsocketSecurityException();
        }
    }

    private boolean isApplyUri(String destination) {
        return !antPathMatcher.match("/wait-service/waitroom/**/**/join", destination);
    }

    private void validateUserAccessor(String parseUserId, String headerUserId) {
        if (!parseUserId.equals(headerUserId)) throw new WebsocketSecurityException();
    }
}
