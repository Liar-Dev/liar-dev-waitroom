package liar.waitservice.wait.repository.redis;

import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.domain.WaitRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RedisTemplateTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    WaitRoomRedisRepository waitRoomRedisRepository;

    WaitRoom waitRoom;

    @BeforeEach
    public void init() {
        waitRoom = WaitRoom.of(new CreateWaitRoomRequest("koseId", "koseRoomName", 7), "koseUsername");
    }

    @Test
    @DisplayName("waitRoom의 정보를 <String, Object> value로 저장하기")
    public void saveOpsValue() throws Exception {
        redisTemplate.opsForValue().set(waitRoom.getId(), waitRoom);
    }

    @Test
    @DisplayName("waitRoom 정보를 <String, Object> Hash로 저장하기")
    public void saveOpsHash() throws Exception {
        redisTemplate.opsForHash().put(waitRoom.getId(), waitRoom.getId(), waitRoom);
    }



}