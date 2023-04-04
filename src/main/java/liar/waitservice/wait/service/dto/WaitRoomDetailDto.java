package liar.waitservice.wait.service.dto;

import liar.waitservice.wait.domain.WaitRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WaitRoomDetailDto {
    private String hostId;
    private String waitRoomId;
    private String waitRoomName;
    private List<String> userNames;
    private int limitMemberCnt;
    private int nowMemberCnt;

    @Builder
    public WaitRoomDetailDto(String hostId, String waitRoomId, String waitRoomName,
                             List<String> userNames, int limitMemberCnt, int nowMemberCnt) {
        this.hostId = hostId;
        this.waitRoomId = waitRoomId;
        this.waitRoomName = waitRoomName;
        this.userNames = userNames;
        this.limitMemberCnt = limitMemberCnt;
        this.nowMemberCnt = nowMemberCnt;
    }

    public static WaitRoomDetailDto of(WaitRoom waitRoom, List<String> userNames) {
        return WaitRoomDetailDto.builder()
                .hostId(waitRoom.getHostId())
                .waitRoomId(waitRoom.getId())
                .waitRoomName(waitRoom.getRoomName())
                .userNames(userNames)
                .limitMemberCnt(waitRoom.getLimitMembers())
                .nowMemberCnt(waitRoom.getMembers().size())
                .build();
    }
}
