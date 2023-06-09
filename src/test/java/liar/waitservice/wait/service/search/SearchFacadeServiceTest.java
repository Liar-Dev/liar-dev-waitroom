package liar.waitservice.wait.service.search;

import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.controller.dto.SearchWaitRoomRequest;
import liar.waitservice.wait.domain.WaitRoom;
import liar.waitservice.wait.domain.utils.SearchType;
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
class SearchFacadeServiceTest {

    @Autowired
    SearchFacadeService searchFacadeService;

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
    @DisplayName("hostName 타입으로 검색하기")
    public void searchWaitRoomCondition_ByHostName() throws Exception {
        for (int i = 0; i < 10; i++) {
            WaitRoom otherWaitRoom = WaitRoom.of(new CreateWaitRoomRequest(String.valueOf(i), String.valueOf(i), 7), "koseUsername");
            waitRoomRedisRepository.save(otherWaitRoom);
            joinMemberRedisRepository.save(otherWaitRoom.createJoinMember());
        }

        //when
        List<WaitRoomViewsDto> waitRoomViewsDtos = searchFacadeService.searchWaitRoomCondition(new SearchWaitRoomRequest("koseUsername", SearchType.HOSTNAME.getTypeName()));

        //then
        assertThat(waitRoomViewsDtos.size()).isEqualTo(11);
        assertThat(waitRoomViewsDtos.get(0).getHostName()).isEqualTo("koseUsername");
        assertThat(waitRoomViewsDtos.get(1).getHostName()).isEqualTo("koseUsername");
        assertThat(waitRoomViewsDtos.get(2).getHostName()).isEqualTo("koseUsername");
        assertThat(waitRoomViewsDtos.get(2).getHostName()).isEqualTo("koseUsername");
        assertThat(waitRoomViewsDtos.get(3).getRoomName()).isNotEqualTo(waitRoomViewsDtos.get(4).getRoomName());
    }

    @Test
    @DisplayName("RoomName 타입으로 검색하기")
    public void searchWaitRoomCondition_ByRoomName() throws Exception {
        for (int i = 0; i < 10; i++) {
            WaitRoom otherWaitRoom = WaitRoom.of(new CreateWaitRoomRequest(String.valueOf(i), "koseRoomName", 7), String.valueOf(i));
            waitRoomRedisRepository.save(otherWaitRoom);
            joinMemberRedisRepository.save(otherWaitRoom.createJoinMember());
        }

        //when
        List<WaitRoomViewsDto> waitRoomViewsDtos = searchFacadeService.searchWaitRoomCondition(new SearchWaitRoomRequest("koseRoomName", SearchType.WAITROOMNAME.getTypeName()));

        //then
        assertThat(waitRoomViewsDtos.size()).isEqualTo(11);
        assertThat(waitRoomViewsDtos.get(0).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(1).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(2).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(2).getRoomName()).isEqualTo("koseRoomName");
        assertThat(waitRoomViewsDtos.get(4).getHostName()).isNotEqualTo(waitRoomViewsDtos.get(7).getHostName());
        assertThat(waitRoomViewsDtos.get(5).getHostName()).isNotEqualTo(waitRoomViewsDtos.get(8).getHostName());
        assertThat(waitRoomViewsDtos.get(6).getHostName()).isNotEqualTo(waitRoomViewsDtos.get(9).getHostName());
    }

    @Test
    @DisplayName("RoomName 타입으로 검색하기")
    public void searchWaitRoomCondition_ByRoomId() throws Exception {
        for (int i = 0; i < 10; i++) {
            WaitRoom otherWaitRoom = WaitRoom.of(new CreateWaitRoomRequest(String.valueOf(i), "koseRoomName", 7), String.valueOf(i));
            waitRoomRedisRepository.save(otherWaitRoom);
            joinMemberRedisRepository.save(otherWaitRoom.createJoinMember());
        }

        //when
        List<WaitRoomViewsDto> waitRoomViewsDtos = searchFacadeService.searchWaitRoomCondition(new SearchWaitRoomRequest(waitRoom.getId(), SearchType.WAITROOMID.name()));

        //then
        assertThat(waitRoomViewsDtos.size()).isEqualTo(1);
        assertThat(waitRoomViewsDtos.get(0).getHostId()).isEqualTo(waitRoom.getHostId());
    }

}