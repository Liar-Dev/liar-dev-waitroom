package liar.waitservice.wait.controller.socket;

import liar.waitservice.exception.exception.BadRequestException;
import liar.waitservice.exception.exception.NotFoundWaitRoomException;
import liar.waitservice.common.other.domain.Member;
import liar.waitservice.wait.controller.dto.CommonWaitRoomRequest;
import liar.waitservice.wait.controller.dto.message.message.ChatMessageResponse;
import liar.waitservice.wait.controller.handler.WaitRoomSocketHandler;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import static liar.waitservice.wait.controller.dto.message.message.JoinStatus.JOIN;
import static liar.waitservice.wait.controller.dto.message.message.JoinStatus.LEAVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8081"})
@AutoConfigureMockMvc
class WaitRoomSocketHandlerTest extends SocketMockMvcController {

    @Autowired
    private WaitRoomSocketHandler waitRoomSocketHandler;

    @Autowired
    WaitRoomFacadeService waitRoomFacadeService;

    private StompHeaderAccessor stompHeaderAccessor;

    @BeforeEach
    public void init() {
        stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        stompHeaderAccessor.addNativeHeader("Authorization", "Bearer " + accessToken);
        stompHeaderAccessor.addNativeHeader("RefreshToken", refreshToken);
        stompHeaderAccessor.addNativeHeader("UserId", userId);
        stompHeaderAccessor.addNativeHeader("WaitRoomId", waitRoomId);
    }

    /**
     * Join Test
     */
    @Test
    @DisplayName("/waitroom/pub/{waitRoomId}/join")
    public void joinTest() throws Exception {
        //given
        CommonWaitRoomRequest request = new CommonWaitRoomRequest(userId, waitRoomId);

        //when
        ChatMessageResponse expectResult = ChatMessageResponse.of(userId, JOIN, true);
        ChatMessageResponse actualResult = waitRoomSocketHandler.joinMember(request, waitRoomId, userId, stompHeaderAccessor);

        //then
        System.out.println("actualResult = " + actualResult.getBody());
        assertThat(expectResult.getBody()).isEqualTo(actualResult.getBody());

    }

    @Test
    @DisplayName("/waitroom/pub/{waitRoomId}/delete")
    public void deleteTest() throws Exception {
        //given
        CommonWaitRoomRequest request = new CommonWaitRoomRequest(userId, waitRoomId);
        waitRoomSocketHandler.joinMember(request, waitRoomId, userId, stompHeaderAccessor);

        //when
        ChatMessageResponse expectedResult = ChatMessageResponse.of(userId, LEAVE, true);
        ChatMessageResponse actualResult = waitRoomSocketHandler.deleteWaitRoom(request, waitRoomId, userId, stompHeaderAccessor);

        //then
        assertThat(expectedResult.getBody()).isEqualTo(actualResult.getBody());
    }

    @Test
    @DisplayName("호스트가 아닌 다른 유저 방 참여하면 message 발급")
    public void joinTest_With_OtherUser() throws Exception {
        //given
        CommonWaitRoomRequest request = new CommonWaitRoomRequest(userId, waitRoomId);
        waitRoomSocketHandler.joinMember(request, waitRoomId, userId, stompHeaderAccessor);

        //when
        String otherUserId = "otherUserId";
        Member member = new Member(otherUserId, "others");
        memberRepository.save(member);

        CommonWaitRoomRequest otherRequest = new CommonWaitRoomRequest(member.getUserId(), waitRoomId);
        ChatMessageResponse expectedResult = ChatMessageResponse.of(userId, JOIN, true);
        ChatMessageResponse actualResult = waitRoomSocketHandler.joinMember(otherRequest, waitRoomId, otherUserId, stompHeaderAccessor);

        //then -> 이미 존재하는 유저는 isJoinMember Exception 발생
        assertThat(expectedResult.getBody()).isEqualTo(actualResult.getBody());
        assertThatThrownBy(() -> {waitRoomFacadeService.isJoinedMemberThenThrow(waitRoomId, otherUserId);})
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("만약 만석보다 더 많은 유저가 참여할 경우 예외 발생")
    public void joinTest_Fail_By_MoreUser() throws Exception {
        //given
        CommonWaitRoomRequest request = new CommonWaitRoomRequest(userId, waitRoomId);
        waitRoomSocketHandler.joinMember(request, waitRoomId, userId, stompHeaderAccessor);

        //when
        for (int i = 0; i < 4; i++) {
            String otherUserId = "otherUserId-" + i;
            Member member = new Member(otherUserId, "others");
            memberRepository.save(member);

            CommonWaitRoomRequest otherRequest = new CommonWaitRoomRequest(member.getUserId(), waitRoomId);
            ChatMessageResponse expectedResult = ChatMessageResponse.of(userId, JOIN, true);
            ChatMessageResponse actualResult = waitRoomSocketHandler.joinMember(otherRequest, waitRoomId, otherUserId, stompHeaderAccessor);
            assertThat(expectedResult.getBody()).isEqualTo(actualResult.getBody());
        }

        String otherUserId = "otherUserId-" + 5;
        Member member = new Member(otherUserId, "others");
        memberRepository.save(member);

        CommonWaitRoomRequest otherRequest = new CommonWaitRoomRequest(member.getUserId(), waitRoomId);

        //then -> 이미 존재하는 유저는 isJoinMember Exception 발생
        assertThatThrownBy(() -> {waitRoomSocketHandler.joinMember(otherRequest, waitRoomId, otherUserId, stompHeaderAccessor);})
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("호스트가 방을 나가면 방 메세지는 LEAVE")
    public void leaveTest() throws Exception {
        //given
        String otherUserId = "otherUserId";
        CommonWaitRoomRequest request = new CommonWaitRoomRequest(userId, waitRoomId);
        waitRoomSocketHandler.joinMember(request, waitRoomId, userId, stompHeaderAccessor);

        Member member = new Member(otherUserId, "others");
        memberRepository.save(member);

        CommonWaitRoomRequest otherRequest = new CommonWaitRoomRequest(member.getUserId(), waitRoomId);
        waitRoomSocketHandler.joinMember(otherRequest, waitRoomId, otherUserId, stompHeaderAccessor);

        //when
        ChatMessageResponse expectedResult = ChatMessageResponse.of(userId, LEAVE, true);
        ChatMessageResponse actualResult = waitRoomSocketHandler.deleteWaitRoom(request, waitRoomId, userId, stompHeaderAccessor);

        //then
        assertThat(expectedResult.getBody()).isEqualTo(actualResult.getBody());
        Assertions.assertThatThrownBy(() -> {
            waitRoomFacadeService.isJoinedMemberThenThrow(waitRoomId, otherUserId);}).isInstanceOf(NotFoundWaitRoomException.class);
    }
}