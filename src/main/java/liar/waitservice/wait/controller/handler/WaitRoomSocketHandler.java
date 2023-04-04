package liar.waitservice.wait.controller.handler;

import jakarta.validation.Valid;
import liar.waitservice.exception.exception.CommonException;
import liar.waitservice.exception.exception.NotFoundUserException;
import liar.waitservice.exception.exception.NotFoundWaitRoomException;
import liar.waitservice.exception.exception.WebsocketSecurityException;
import liar.waitservice.wait.controller.dto.CommonWaitRoomRequest;
import liar.waitservice.wait.controller.dto.message.message.ChatMessageResponse;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

import static liar.waitservice.wait.controller.dto.message.message.JoinStatus.JOIN;
import static liar.waitservice.wait.controller.dto.message.message.JoinStatus.LEAVE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WaitRoomSocketHandler {

    private final WaitRoomFacadeService waitRoomFacadeService;
    private final CustomWebSocketHandlerDecorator decorator;

    /**
     * StompHeaderAccessor의 필수 헤더
     * {@code @Header} Authorization: 인증 accessToken
     * {@code @Header} RefreshToken:  인증 refreshToken
     * {@code @Header} UserId: 요청 userId
     * {@code @Header} WaitRoomId: 요청 waitRoomId
     */
    @MessageMapping("/waitroom/pub/{waitRoomId}/join")
    @SendTo("/wait-service/waitroom/sub/{waitRoomId}/join")
    public ChatMessageResponse joinMember(@Valid @RequestBody CommonWaitRoomRequest dto,
                           @DestinationVariable String waitRoomId,
                           @Header("UserId") String userId,
                           StompHeaderAccessor stompHeaderAccessor) {

        if (!userId.equals(dto.getUserId())) throw new WebsocketSecurityException();
        return ChatMessageResponse.of(dto.getUserId(), JOIN, waitRoomFacadeService.addMembers(dto));
    }

    @MessageMapping("/waitroom/pub/{waitRoomId}/delete")
    @SendTo("/wait-service/waitroom/sub/{waitRoomId}/delete")
    public ChatMessageResponse deleteWaitRoom(@Valid @RequestBody CommonWaitRoomRequest dto,
                               @DestinationVariable String waitRoomId,
                               @Header("UserId") String userId,
                               StompHeaderAccessor stompHeaderAccessor) {

        if (!userId.equals(dto.getUserId())) throw new WebsocketSecurityException();
        return ChatMessageResponse.of(dto.getUserId(), LEAVE, waitRoomFacadeService.deleteWaitRoomByHost(dto));
    }

    @MessageMapping("/waitroom/pub/{waitRoomId}/leave")
    @SendTo("/wait-service/waitroom/sub/{waitRoomId}/leave")
    public ChatMessageResponse leaveMember(@Valid @RequestBody CommonWaitRoomRequest dto,
                            @DestinationVariable String waitRoomId,
                            @Header("UserId") String userId,
                            StompHeaderAccessor stompHeaderAccessor) {

        if (!userId.equals(dto.getUserId())) throw new WebsocketSecurityException();
        boolean leaveStatus = waitRoomFacadeService.leaveMember(dto);
        return ChatMessageResponse.of(dto.getUserId(), LEAVE, leaveStatus);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception, StompHeaderAccessor stompHeaderAccessor) throws IOException {

        if (exception instanceof WebsocketSecurityException ||
                exception instanceof NotFoundWaitRoomException ||
                exception instanceof NotFoundUserException) {
            String sessionId = stompHeaderAccessor.getSessionId();
            log.info("session = {}, connection remove", sessionId);
            decorator.closeSession(sessionId);
        }
        else if (exception instanceof CommonException) {
            return "server exception: " + exception.getMessage();
        }
        else {
            String sessionId = stompHeaderAccessor.getSessionId();
            decorator.closeSession(sessionId);
        }

        return "server exception: " + exception.getMessage() + "server session clear";
    }

}
