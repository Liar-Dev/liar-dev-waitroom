package liar.waitservice.wait.service.start;

import jakarta.ws.rs.NotFoundException;
import liar.waitservice.exception.exception.NotEqualHostIdException;
import liar.waitservice.exception.exception.NotFoundWaitRoomException;
import liar.waitservice.exception.exception.NotSatisfiedMinJoinMembers;
import liar.waitservice.wait.MemberDummyInfo;
import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.controller.dto.PostProcessEndGameRequest;
import liar.waitservice.wait.controller.dto.CommonWaitRoomRequest;
import liar.waitservice.wait.domain.JoinMember;
import liar.waitservice.wait.domain.WaitRoom;
import liar.waitservice.wait.domain.WaitRoomComplete;
import liar.waitservice.wait.domain.utils.WaitRoomCompleteStatus;
import liar.waitservice.wait.repository.rdbms.WaitRoomCompleteRepository;
import liar.waitservice.wait.repository.redis.JoinMemberRedisRepository;
import liar.waitservice.wait.repository.redis.WaitRoomRedisRepository;
import liar.waitservice.wait.service.waitroom.WaitRoomServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DoProcessStartAndEndGameServiceImplTest extends MemberDummyInfo {

    @Autowired
    WaitRoomCompleteRepository waitRoomCompleteRepository;

    @Autowired
    WaitRoomRedisRepository waitRoomRedisRepository;

    @Autowired
    JoinMemberRedisRepository joinMemberRedisRepository;

    @Autowired
    DoProcessStartAndEndGameServiceImpl updateWaitRoomStatusService;

    @Autowired
    WaitRoomServiceImpl waitRoomCompleteServiceImpl;

    WaitRoom waitRoom;

    @BeforeEach
    public void init() {
        waitRoom = WaitRoom.of(new CreateWaitRoomRequest(hostId, "koseRoom", 5), "koseName");
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(waitRoom.createJoinMember());
    }

    @AfterEach
    public void tearDown() {
        waitRoomRedisRepository.deleteAll();
        joinMemberRedisRepository.deleteAll();
    }
    
    @Test
    @DisplayName("RequestWaitRoomDto로 요청된 정보가 호스트고 최소 인원을 만족하면, redis에 저장된 waitRoom을 WaitRoomComplete로 RDMBS에 저장하기")
    public void doPreProcessBeforeGameStart_success() throws Exception {

        //given
        waitRoom.joinMembers(devUser1Id);
        waitRoom.joinMembers(devUser2Id);
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(JoinMember.of(devUser1Id, waitRoom.getId()));
        joinMemberRedisRepository.save(JoinMember.of(devUser2Id, waitRoom.getId()));

        CommonWaitRoomRequest commonWaitRoomRequest = new CommonWaitRoomRequest(waitRoom.getHostId(), waitRoom.getId());

        //when
        updateWaitRoomStatusService.doPreProcessBeforeGameStart(commonWaitRoomRequest);
        WaitRoomComplete waitRoomComplete = waitRoomCompleteRepository.findWaitRoomCompleteByWaitRoomId(waitRoom.getId()).orElseThrow(NotFoundException::new);

        //then
        assertThat(waitRoomComplete.getHostName()).isEqualTo(waitRoom.getHostName());
        assertThat(waitRoomComplete.getHostId()).isEqualTo(waitRoom.getHostId());
        assertThat(waitRoomComplete.getLimitMembers()).isEqualTo(waitRoom.getLimitMembers());
        assertThat(waitRoomComplete.getWaitRoomCompleteStatus()).isEqualTo(WaitRoomCompleteStatus.PLAYING);

    }

    @Test
    @DisplayName("호스트가 아니라면, doPreProcessBeforeGameStart 요청은 실패한다.")
    public void doPreProcessBeforeGameStart_fail_becauseNotHost() throws Exception {

        //given
        waitRoom.joinMembers(devUser1Id);
        waitRoom.joinMembers(devUser2Id);
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(JoinMember.of(devUser1Id, waitRoom.getId()));
        joinMemberRedisRepository.save(JoinMember.of(devUser2Id, waitRoom.getId()));

        //when
        CommonWaitRoomRequest commonWaitRoomRequest = new CommonWaitRoomRequest(devUser1Id, waitRoom.getId());

        //then
        assertThatThrownBy(() -> updateWaitRoomStatusService.doPreProcessBeforeGameStart(commonWaitRoomRequest))
                .isInstanceOf(NotEqualHostIdException.class);

    }

    @Test
    @DisplayName("최소 인원을 만족하지 못하면, doPreProcessBeforeGameStart 요청은 실패한다.")
    public void doPreProcessBeforeGameStart_fail_becauseNotSatisfiedjoinMembers() throws Exception {
        //given
        waitRoom.joinMembers(devUser1Id);
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(JoinMember.of(devUser1Id, waitRoom.getId()));

        // when
        CommonWaitRoomRequest commonWaitRoomRequest = new CommonWaitRoomRequest(waitRoom.getHostId(), waitRoom.getId());

        //then
        assertThatThrownBy(() -> updateWaitRoomStatusService.doPreProcessBeforeGameStart(commonWaitRoomRequest))
                .isInstanceOf(NotSatisfiedMinJoinMembers.class);

    }

    @Test
    @DisplayName("요청된 대기실 정보가 없다면, doPreProcessBeforeGameStart 요청은 실패한다.")
    public void doPreProcessBeforeGameStart_fail_becauseNotFoundRoom() throws Exception {

        //given
        waitRoom.joinMembers(devUser1Id);
        waitRoom.joinMembers(devUser2Id);
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(JoinMember.of(devUser1Id, waitRoom.getId()));
        joinMemberRedisRepository.save(JoinMember.of(devUser2Id, waitRoom.getId()));

        //when
        CommonWaitRoomRequest commonWaitRoomRequest = new CommonWaitRoomRequest(hostId, "???");

        //then
        assertThatThrownBy(() -> updateWaitRoomStatusService.doPreProcessBeforeGameStart(commonWaitRoomRequest))
                .isInstanceOf(NotFoundWaitRoomException.class);

    }

    @Test
    @DisplayName("deleteWaitRoomFromRedis는 redis에 있는 waitRoom과 joinMembers의 정보를 제거하고, waitRoomComplete 정보를 End로 수정한다.")
    public void doPreProcessBeforeGameStart() throws Exception {
        //given
        waitRoom.joinMembers(devUser1Id);
        waitRoom.joinMembers(devUser2Id);
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(JoinMember.of(devUser1Id, waitRoom.getId()));
        joinMemberRedisRepository.save(JoinMember.of(devUser2Id, waitRoom.getId()));

        CommonWaitRoomRequest commonWaitRoomRequest = new CommonWaitRoomRequest(hostId, waitRoom.getId());
        updateWaitRoomStatusService.doPreProcessBeforeGameStart(commonWaitRoomRequest);


        //when
        PostProcessEndGameRequest postProcessEndGameRequest = new PostProcessEndGameRequest<String>(waitRoom.getId());
        updateWaitRoomStatusService.doPostProcessAfterGameEnd(postProcessEndGameRequest);
        WaitRoomComplete waitRoomComplete = waitRoomCompleteServiceImpl.findWaitRoomCompleteByWaitRoomId(waitRoom.getId());

        //then
        assertThat(waitRoomComplete.getWaitRoomCompleteStatus()).isEqualTo(WaitRoomCompleteStatus.END);
        assertThatThrownBy(() -> joinMemberRedisRepository.findById(hostId).orElseThrow(NotFoundException::new));
        assertThatThrownBy(() -> joinMemberRedisRepository.findById(devUser1Id).orElseThrow(NotFoundException::new));
        assertThatThrownBy(() -> joinMemberRedisRepository.findById(devUser2Id).orElseThrow(NotFoundException::new));
        assertThatThrownBy(() -> waitRoomRedisRepository.findById(waitRoom.getId()).orElseThrow(NotFoundException::new));
    }





}