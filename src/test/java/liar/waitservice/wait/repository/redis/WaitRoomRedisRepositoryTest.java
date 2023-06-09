package liar.waitservice.wait.repository.redis;

import liar.waitservice.exception.exception.NotFoundWaitRoomException;
import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.domain.WaitRoom;
import liar.waitservice.wait.repository.rdbms.WaitRoomCompleteRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaitRoomRedisRepositoryTest {

    @Autowired
    WaitRoomRedisRepository waitRoomRedisRepository;
    private WaitRoom waitRoom;
    @Autowired
    private WaitRoomCompleteRepository waitRoomCompleteRepository;

    @BeforeEach
    void init() {
        CreateWaitRoomRequest roomDto = new CreateWaitRoomRequest("kose", "koseTest1", 5);
        waitRoom = WaitRoom.of(roomDto, "kose");
    }

    @AfterEach
    void tearDown() {
        waitRoomRedisRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis에 createWaitRoom 요청이 오면, 저장되어야 한다.")
    public void saveWaitRoom() throws Exception {
        //given
        WaitRoom save = waitRoomRedisRepository.save(waitRoom);
        System.out.println("save = " + save);

        //when
        WaitRoom findWaitRoom = findById(waitRoom.getId());

        //then
        assertThat(findWaitRoom.getRoomName()).isEqualTo("koseTest1");
        assertThat(findWaitRoom.getHostId()).isEqualTo("kose");
        assertThat(findWaitRoom.getLimitMembers()).isEqualTo(5);
        assertThat(findWaitRoom.getMembers().size()).isEqualTo(1);
    }


    @Test
    @DisplayName("대기방에 입장 요청이 오면, 인원을 추가하여 값을 변경하여 저장 해야 한다.")
    public void joinMembers() throws Exception {
        //given
        waitRoomRedisRepository.save(waitRoom);
        WaitRoom findRoom = findById(waitRoom.getId());

        CreateWaitRoomRequest roomDto = new CreateWaitRoomRequest("kosett", "koseTest1", 5);
        WaitRoom waitRoom1 = WaitRoom.of(roomDto, "kosettt");
        waitRoomRedisRepository.save(waitRoom1);

        //when
        findRoom.joinMembers("kose2");
        findRoom.joinMembers("kose3");
        findRoom.joinMembers("kose4");
        waitRoomRedisRepository.save(findRoom);
        WaitRoom result = findById(findRoom.getId());

        //then
        assertThat(result.getRoomName()).isEqualTo("koseTest1");
        assertThat(result.getHostId()).isEqualTo("kose");
        assertThat(result.getLimitMembers()).isEqualTo(5);
        assertThat(result.getMembers().size()).isEqualTo(4);
        assertThat(result.getMembers().get(1)).isEqualTo("kose2");
        assertThat(result.getMembers().stream().filter(f -> f.startsWith("kose")).collect(Collectors.toList()).size()).isEqualTo(4);
    }

    @Test
    @DisplayName("대기방에 있던 유저가 퇴장하면, 인원 변동되어 저장해야 한다.")
    public void leaveMembers() throws Exception {
        //given
        waitRoomRedisRepository.save(waitRoom);
        WaitRoom findRoom = findById(waitRoom.getId());
        findRoom.joinMembers("kose2");
        findRoom.joinMembers("kose3");
        findRoom.joinMembers("kose4");
        waitRoomRedisRepository.save(findRoom);

        //when
        WaitRoom room = findById(waitRoom.getId());
        room.leaveMember("kose2");
        room.leaveMember("kose4");
        WaitRoom result = waitRoomRedisRepository.save(room);

        //then
        assertThat(result.getRoomName()).isEqualTo("koseTest1");
        assertThat(result.getHostId()).isEqualTo("kose");
        assertThat(result.getLimitMembers()).isEqualTo(5);
        assertThat(result.getMembers().size()).isEqualTo(2);
        assertThat(result.getMembers().get(1)).isEqualTo("kose3");
    }

    @Test
    @DisplayName("대기방에 유저가 만석이 되면, 인원 입장이 불가하다")
    public void joinMembersDisable() throws Exception {
        //given
        waitRoomRedisRepository.save(waitRoom);
        WaitRoom findRoom = findById(waitRoom.getId());
        findRoom.joinMembers("kose2");
        findRoom.joinMembers("kose3");
        findRoom.joinMembers("kose4");
        findRoom.joinMembers("kose5");
        waitRoomRedisRepository.save(findRoom);

        //when
        boolean isJoinMember = findRoom.joinMembers("kose6");
        waitRoomRedisRepository.save(findRoom);
        WaitRoom result = findById(waitRoom.getId());

        //then
        assertThat(result.getRoomName()).isEqualTo("koseTest1");
        assertThat(result.getHostId()).isEqualTo("kose");
        assertThat(result.getLimitMembers()).isEqualTo(5);
        assertThat(result.getMembers().size()).isEqualTo(5);
        assertThat(result.getMembers().get(4)).isEqualTo("kose5");
        assertThat(!isJoinMember);
    }

    @Test
    @DisplayName("호스트가 대기방을 나가면, 방이 종료된다.")
    public void leaveHost() throws Exception {
        //given
        waitRoomRedisRepository.save(waitRoom);
        WaitRoom findRoom = findById(waitRoom.getId());
        findRoom.joinMembers("kose2");
        findRoom.joinMembers("kose3");
        findRoom.joinMembers("kose4");
        findRoom.joinMembers("kose5");
        WaitRoom result = waitRoomRedisRepository.save(findRoom);

        //when
        waitRoomRedisRepository.delete(result);

        //then
        Assertions.assertThatThrownBy(() -> findById(result.getId()))
                .isInstanceOf(NotFoundWaitRoomException.class);
    }

    @Test
    @DisplayName("hostName(인덱스)로 Room정보 가져오기")
    public void getWaitRoomByHostName() throws Exception {
        //given
        WaitRoom savedWaitRoom = waitRoomRedisRepository.save(waitRoom);

        //when
        List<WaitRoom> findAll = waitRoomRedisRepository.findAllByHostName(waitRoom.getHostName());

        //then
        assertThat(findAll.get(0).getId()).isEqualTo(waitRoom.getId());

    }

    @Test
    @DisplayName("roomName(인덱스)로 Room 찾기")
    public void getWaitRoomByRoomName() throws Exception {
        //given
        WaitRoom saveWaitRoom = waitRoomRedisRepository.save(waitRoom);

        //when
        List<WaitRoom> findAll = waitRoomRedisRepository.findAllByRoomName(waitRoom.getRoomName());

        //then
        assertThat(findAll.get(0).getId()).isEqualTo(waitRoom.getId());
    }

    @Test
    @DisplayName("test")
    public void test() throws Exception {
        //given
        for (int i = 0; i < 30; i++) {
            waitRoomRedisRepository.save(
                    WaitRoom.of(new CreateWaitRoomRequest(
                            "user" + i, "koseRoomName", 7), "koseUsername"));
        }

        //when
        Slice<WaitRoom> koseRoomName = waitRoomRedisRepository.findWaitRoomByRoomName("koseRoomName", PageRequest.of(0, 10));

        //then
        for (WaitRoom room : koseRoomName.getContent()) {
            System.out.println("room = " + room.getId());
        }

        assertThat(koseRoomName.getSize()).isEqualTo(10);

        Slice<WaitRoom> koseRoomName1 = waitRoomRedisRepository.findWaitRoomByRoomName("koseRoomName", PageRequest.of(1, 10));

        //then
        for (WaitRoom room : koseRoomName1.getContent()) {
            System.out.println("room = " + room.getId());
        }

        assertThat(koseRoomName.getSize()).isEqualTo(10);
    }


    @Test
    @DisplayName("pageable test")
    public void findAll() throws Exception {
        //given
        for (int i = 0; i < 30; i++) {
            waitRoomRedisRepository.save(
                    WaitRoom.of(new CreateWaitRoomRequest(
                            "user" + i, "koseRoomName", 7), "koseUsername"));
        }

        //when
        Slice<WaitRoom> allBy = waitRoomRedisRepository.findAllBy(PageRequest.of(0, 10));

        //then
        WaitRoom findWaitRoom = allBy.getContent().get(0);

        assertThat(findWaitRoom.getCreatedAt()).isNotNull();
        assertThat(findWaitRoom.getMembers()).isNotNull();
        assertThat(findWaitRoom.getLimitMembers()).isNotNull();
        assertThat(findWaitRoom.getHostId()).isNotNull();
        assertThat(findWaitRoom.getHostName()).isNotNull();
    }



    private WaitRoom findById(String id) {
        return waitRoomRedisRepository.findById(waitRoom.getId()).orElseThrow(NotFoundWaitRoomException::new);
    }

}