package liar.waitservice.wait.domain;

import jakarta.persistence.Id;
import liar.waitservice.wait.controller.dto.CommonWaitRoomRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("JoinMember")
public class JoinMember {

    @Id
    private String id;
    private String roomId;

    public JoinMember(String userId, String roomId) {
        this.id = userId;
        this.roomId = roomId;
    }

    public static JoinMember of(String userId, String roomId) {
        return new JoinMember(userId, roomId);
    }
    public static JoinMember of(CommonWaitRoomRequest joinDto) { return new JoinMember(joinDto.getUserId(), joinDto.getRoomId());}
}
