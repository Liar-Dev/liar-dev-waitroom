package liar.waitservice.wait.service.search;

import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.domain.JoinMember;
import liar.waitservice.wait.domain.WaitRoom;
import liar.waitservice.wait.repository.redis.JoinMemberRedisRepository;
import liar.waitservice.wait.repository.redis.WaitRoomRedisRepository;
import liar.waitservice.wait.service.search.dto.WaitRoomViewsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchRoomNameServiceTest {

    @Autowired
    SearchRoomNameService searchRoomNameService;

    @Autowired
    WaitRoomRedisRepository waitRoomRedisRepository;

    @Autowired
    JoinMemberRedisRepository joinMemberRedisRepository;

    WaitRoom waitRoom;

    @BeforeEach
    public void init() {
        waitRoom = WaitRoom.of(new CreateWaitRoomRequest("koseId", "koseRoomName", 7), "koseUsername");
        waitRoomRedisRepository.save(waitRoom);
        joinMemberRedisRepository.save(waitRoom.createJoinMember());
    }

    @AfterEach
    public void tearDown() {
        waitRoomRedisRepository.deleteAll();
        joinMemberRedisRepository.deleteAll();
    }

    @Test
    @DisplayName("RoomName으로 Room 찾기")
    public void searchWaitRoomByRoomName() throws Exception {
        //given-when
        String roomName = "koseRoomName";

        //when
        List<WaitRoomViewsDto> waitRoomViewsDtos = searchRoomNameService.searchWaitRoomByCond(roomName);

        //then
        assertThat(waitRoomViewsDtos.size()).isEqualTo(1);
        assertThat(waitRoomViewsDtos.get(0).getHostId()).isEqualTo(waitRoom.getHostId());
        assertThat(waitRoomViewsDtos.get(0).getHostName()).isEqualTo(waitRoom.getHostName());
        assertThat(waitRoomViewsDtos.get(0).getJoinMembersCnt()).isEqualTo(1);
        assertThat(waitRoomViewsDtos.get(0).getLimitsMembers()).isEqualTo(waitRoom.getLimitMembers());
        assertThat(waitRoomViewsDtos.get(0).getRoomId()).isEqualTo(waitRoom.getId());

    }
    @Test
    @DisplayName("RoomName으로 Room찾기 (방이 여러 개)")
    public void searchWaitRoomByRoomName_manyRoomName() throws Exception {
        //given
        for (int i = 0; i < 10; i++) {
            WaitRoom otherWaitRoom = WaitRoom.of(new CreateWaitRoomRequest(String.valueOf(i), "koseRoomName", 7), "koseUsername");
            waitRoomRedisRepository.save(otherWaitRoom);
            joinMemberRedisRepository.save(otherWaitRoom.createJoinMember());
        }

        //when
        List<WaitRoomViewsDto> waitRoomViewsDtos = searchRoomNameService.searchWaitRoomByCond("koseRoomName");

        //then
        assertThat(waitRoomViewsDtos.size()).isEqualTo(11);
        assertThat(waitRoomViewsDtos.get(0).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(1).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(2).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(2).getHostName()).isEqualTo("koseUsername");
    }

}